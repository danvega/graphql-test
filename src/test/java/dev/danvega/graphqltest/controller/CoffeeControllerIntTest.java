package dev.danvega.graphqltest.controller;

import dev.danvega.graphqltest.model.Coffee;
import dev.danvega.graphqltest.model.Size;
import dev.danvega.graphqltest.service.CoffeeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@GraphQlTest(CoffeeController.class)
@Import(CoffeeService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoffeeControllerIntTest {

    @Autowired
    GraphQlTester graphQlTester;

    @Autowired
    CoffeeService coffeeService;

    @Test
    @Order(1)
    void testFindAllCoffeeShouldReturnAllCoffees() {

        // language=GraphQL
        String document = """
        query {
            findAll {
                id
                name
                size
            }
        }            
        """;

        graphQlTester.document(document)
                .execute()
                .path("findAll")
                .entityList(Coffee.class)
                .hasSize(3);
    }

    @Test
    @Order(2)
    void validIdShouldReturnCoffee() {
        // language=GraphQL
        String document = """
        query findOneCoffee($id: ID){
            findOne(id: $id) {
                id
                name
                size
            }
        }            
        """;

        graphQlTester.document(document)
                .variable("id", 1)
                .execute()
                .path("findOne")
                .entity(Coffee.class)
                .satisfies(coffee -> {
                    assertEquals("Caffè Americano",coffee.name());
                    assertEquals(Size.GRANDE,coffee.size());
                });

    }

    @Test
    @Order(3)
    void invalidIdShouldReturnNull() {
        // language=GraphQL
        String document = """
        query findOneCoffee($id: ID){
            findOne(id: $id) {
                id
                name
                size
            }
        }            
        """;

        graphQlTester.document(document)
                .variable("id", 99)
                .execute()
                .path("findOne")
                .valueIsNull();

    }

    @Test
    @Order(4)
    void shouldCreateNewCoffee() {
        int currentCoffeeCount = coffeeService.findAll().size();

        // language=GraphQL
        String document = """
            mutation create($name: String, $size: Size) {
                create(name: $name, size: $size) {
                    id
                    name
                    size
                }
            }
        """;

        graphQlTester.document(document)
                .variable("name","Caffè Latte")
                .variable("size", Size.GRANDE)
                .execute()
                .path("create")
                .entity(Coffee.class)
                .satisfies(coffee -> {
                    assertNotNull(coffee.id());
                    assertEquals("Caffè Latte",coffee.name());
                    assertEquals(Size.GRANDE,coffee.size());
                });

        assertEquals(currentCoffeeCount + 1,coffeeService.findAll().size());
    }

    @Test
    @Order(5)
    void shouldUpdateExistingCoffee() {
        Coffee currentCoffee = coffeeService.findOne(1).get();

        // language=GraphQL
        String document = """
            mutation update($id: ID, $name: String, $size: Size) {
                update(id: $id, name: $name, size: $size) {
                    id
                    name
                    size   
                }
            }
        """;

        graphQlTester.document(document)
                .variable("id", 1)
                .variable("name","UPDATED: Caffè Latte")
                .variable("size", Size.SHORT)
                .execute()
                .path("update")
                .entity(Coffee.class);

        Coffee updatedCoffee = coffeeService.findOne(1).get();
        assertEquals("UPDATED: Caffè Latte",updatedCoffee.name());
        assertEquals(Size.SHORT,updatedCoffee.size());
    }

    @Test
    @Order(6)
    void shouldRemoveCoffeeWithValidId() {
        int currentCount = coffeeService.findAll().size();

        // language=GraphQL
        String document = """
            mutation delete($id: ID) {
                delete(id: $id) {
                    id
                    name
                    size
                }
            }
        """;

        graphQlTester.document(document)
                .variable("id", 1)
                .executeAndVerify();

        assertEquals(currentCount - 1, coffeeService.findAll().size());
    }

}