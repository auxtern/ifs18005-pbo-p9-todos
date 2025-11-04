package org.delcom.todos.integrations;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.delcom.todos.entities.Todo;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoIntegrationTests {

    @LocalServerPort
    private int port;

    private static UUID todoId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = port;
    }

    // CREATE ✅ valid
    @Test
    @Order(1)
    @DisplayName("Create Todo - Valid")
    void createTodo_valid() {
        Todo todo = new Todo("Belajar Spring Boot", "Membuat integration test dengan RestAssured", false);

        todoId = given()
                .contentType(ContentType.JSON)
                .body(todo)
                .when()
                .post("/api/todos")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("success"))
                .body("data.id", notNullValue())
                .extract()
                .jsonPath()
                .getUUID("data.id");

        Assertions.assertNotNull(todoId, "todoId harus tidak null setelah pembuatan Todo berhasil");
    }

    // CREATE ❌ invalid
    @Test
    @Order(2)
    @DisplayName("Create Todo - Invalid Inputs")
    void createTodo_invalid() {
        Todo invalidTodo = new Todo("", "", false);

        given()
                .contentType(ContentType.JSON)
                .body(invalidTodo)
                .when()
                .post("/api/todos")
                .then()
                .statusCode(HttpStatus.OK.value()) // Controller return "fail" tapi HTTP 200
                .body("status", equalTo("fail"))
                .body("message", containsString("Data tidak valid"));
    }

    // GET ALL ✅
    @Test
    @Order(3)
    @DisplayName("Get All Todos - Success")
    void getAllTodos() {
        given()
                .when()
                .get("/api/todos")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("success"))
                .body("data.todos", not(empty()));
    }

    // GET BY ID ✅ exists
    @Test
    @Order(4)
    @DisplayName("Get Todo by ID - Exists")
    void getTodoById_exists() {
        given()
                .when()
                .get("/api/todos/{id}", todoId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("success"))
                .body("data.todo.id", equalTo(todoId.toString()));
    }

    // GET BY ID ❌ not found
    @Test
    @Order(5)
    @DisplayName("Get Todo by ID - Not Found")
    void getTodoById_notFound() {
        UUID randomId = UUID.randomUUID();

        given()
                .when()
                .get("/api/todos/{id}", randomId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("fail"))
                .body("message", containsString("tidak ditemukan"));
    }

    // UPDATE ✅ valid
    @Test
    @Order(6)
    @DisplayName("Update Todo - Valid")
    void updateTodo_valid() {
        Todo updated = new Todo("Belajar Spring Boot - Updated", "Deskripsi diperbarui", true);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/api/todos/{id}", todoId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("success"))
                .body("message", containsString("berhasil diperbarui"));
    }

    // UPDATE ❌ invalid
    @Test
    @Order(7)
    @DisplayName("Update Todo - Invalid Inputs")
    void updateTodo_invalid() {
        Todo invalidTodo = new Todo("", "", false);

        given()
                .contentType(ContentType.JSON)
                .body(invalidTodo)
                .when()
                .put("/api/todos/{id}", todoId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("fail"))
                .body("message", containsString("Data tidak valid"));
    }

    // UPDATE ❌ not found
    @Test
    @Order(8)
    @DisplayName("Update Todo - Not Found")
    void updateTodo_notFound() {
        UUID randomId = UUID.randomUUID();
        Todo updated = new Todo("Tidak ada", "ID tidak ditemukan", false);

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/api/todos/{id}", randomId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("fail"))
                .body("message", containsString("tidak ditemukan"));
    }

    // DELETE ✅ exists
    @Test
    @Order(9)
    @DisplayName("Delete Todo - Exists")
    void deleteTodo_exists() {
        Assertions.assertNotNull(todoId, "todoId harus sudah diisi dari createTodo_valid");

        given()
                .when()
                .delete("/api/todos/{id}", todoId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("success"))
                .body("message", containsString("berhasil dihapus"));
    }

    // DELETE ❌ not found
    @Test
    @Order(10)
    @DisplayName("Delete Todo - Not Found")
    void deleteTodo_notFound() {
        UUID randomId = UUID.randomUUID();

        given()
                .when()
                .delete("/api/todos/{id}", randomId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("fail"))
                .body("message", containsString("tidak ditemukan"));
    }
}
