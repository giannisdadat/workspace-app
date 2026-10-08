package com.john.workspace_saas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TenantIsolationTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    record Res(int status, String body) {}

    record TestUser(String email, String token) {}

    // ---------- Tests ----------

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        assertThat(call("GET", "/api/workspaces", null, null).status()).isEqualTo(401);
    }

    @Test
    void outsiderCannotReadAnotherWorkspace() throws Exception {
        TestUser alice = newUser("alice");
        TestUser bob = newUser("bob");
        String workspaceA = createWorkspace(alice, "Alice WS");
        createProject(alice, workspaceA, "Secret");

        Res res = call("GET", "/api/workspaces/" + workspaceA + "/projects", bob.token(), null);

        assertThat(res.status()).isEqualTo(404);
        assertThat(res.body()).doesNotContain("Secret");
    }

    @Test
    void outsiderCannotTouchTaskByGuessingItsId() throws Exception {
        TestUser alice = newUser("alice");
        TestUser bob = newUser("bob");
        String workspaceA = createWorkspace(alice, "Alice WS");
        String projectA = createProject(alice, workspaceA, "Board");
        String taskA = createTask(alice, workspaceA, projectA, "Private task");

        // Ο Bob χρησιμοποιεί ΤΟ ΔΙΚΟ ΤΟΥ workspace αλλά το id του task της Alice
        String workspaceB = createWorkspace(bob, "Bob WS");
        Res patch = call("PATCH", "/api/workspaces/" + workspaceB + "/tasks/" + taskA,
                bob.token(), "{\"status\":\"DONE\"}");
        Res delete = call("DELETE", "/api/workspaces/" + workspaceB + "/tasks/" + taskA,
                bob.token(), null);

        assertThat(patch.status()).isEqualTo(404);
        assertThat(delete.status()).isEqualTo(404);

        // Το task της Alice δεν άλλαξε
        Res list = call("GET", "/api/workspaces/" + workspaceA + "/projects/" + projectA + "/tasks",
                alice.token(), null);
        assertThat(list.body()).contains("Private task").contains("TODO");
    }

    @Test
    void taskCannotBeAttachedToProjectOfAnotherWorkspace() throws Exception {
        TestUser alice = newUser("alice");
        String workspaceA = createWorkspace(alice, "First");
        String workspaceB = createWorkspace(alice, "Second");
        String projectA = createProject(alice, workspaceA, "Board A");

        // Η Alice είναι μέλος και στα δύο, αλλά το project ανήκει στο A
        Res res = call("POST", "/api/workspaces/" + workspaceB + "/projects/" + projectA + "/tasks",
                alice.token(), "{\"title\":\"Wrong place\"}");

        assertThat(res.status()).isEqualTo(404);
    }

    @Test
    void memberCanCreateTasksButNotProjects() throws Exception {
        TestUser alice = newUser("alice");
        TestUser bob = newUser("bob");
        String workspaceA = createWorkspace(alice, "Alice WS");
        String projectA = createProject(alice, workspaceA, "Board");

        Res add = call("POST", "/api/workspaces/" + workspaceA + "/members", alice.token(),
                "{\"email\":\"" + bob.email() + "\",\"role\":\"MEMBER\"}");
        assertThat(add.status()).isEqualTo(201);

        Res newProject = call("POST", "/api/workspaces/" + workspaceA + "/projects", bob.token(),
                "{\"name\":\"Not allowed\"}");
        Res newTask = call("POST", "/api/workspaces/" + workspaceA + "/projects/" + projectA + "/tasks",
                bob.token(), "{\"title\":\"Allowed\"}");

        assertThat(newProject.status()).isEqualTo(403);
        assertThat(newTask.status()).isEqualTo(201);
    }

    // ---------- Βοηθητικά ----------

    private TestUser newUser(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@test.com";
        Res reg = call("POST", "/api/auth/register", null,
                "{\"email\":\"" + email + "\",\"password\":\"12345678\",\"name\":\"" + prefix + "\"}");
        assertThat(reg.status()).isEqualTo(201);

        Res login = call("POST", "/api/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"12345678\"}");
        assertThat(login.status()).isEqualTo(200);
        return new TestUser(email, jsonValue(login.body(), "token"));
    }

    private String createWorkspace(TestUser user, String name) throws Exception {
        Res res = call("POST", "/api/workspaces", user.token(), "{\"name\":\"" + name + "\"}");
        assertThat(res.status()).isEqualTo(201);
        return jsonValue(res.body(), "id");
    }

    private String createProject(TestUser user, String workspaceId, String name) throws Exception {
        Res res = call("POST", "/api/workspaces/" + workspaceId + "/projects", user.token(),
                "{\"name\":\"" + name + "\"}");
        assertThat(res.status()).isEqualTo(201);
        return jsonValue(res.body(), "id");
    }

    private String createTask(TestUser user, String workspaceId, String projectId, String title)
            throws Exception {
        Res res = call("POST", "/api/workspaces/" + workspaceId + "/projects/" + projectId + "/tasks",
                user.token(), "{\"title\":\"" + title + "\"}");
        assertThat(res.status()).isEqualTo(201);
        return jsonValue(res.body(), "id");
    }

    private Res call(String method, String path, String token, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json));

        HttpResponse<String> res = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new Res(res.statusCode(), res.body());
    }

    // Απλή εξαγωγή τιμής από JSON (αρκεί για τα δικά μας flat responses)
    private String jsonValue(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\":\"?([^\",}]+)").matcher(json);
        assertThat(m.find()).as("field '%s' in %s", field, json).isTrue();
        return m.group(1);
    }
}