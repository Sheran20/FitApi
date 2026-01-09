package com.sgt.fitapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import com.sgt.fitapi.controller.AuthController;
import com.sgt.fitapi.controller.ExerciseController;
import com.sgt.fitapi.controller.HelloController;
import com.sgt.fitapi.controller.WorkoutSessionController;
import com.sgt.fitapi.controller.WorkoutSetController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        Schema<?> errorSchema = new Schema<>()
                .type("object")
                .addProperty("timestamp", new StringSchema()
                        .description("Error time in ISO-8601 format.")
                        .example("2025-01-15T12:34:56.789Z"))
                .addProperty("status", new IntegerSchema()
                        .description("HTTP status code.")
                        .example(401))
                .addProperty("error", new StringSchema()
                        .description("Short error reason.")
                        .example("Unauthorized"))
                .addProperty("message", new StringSchema()
                        .description("Detailed error message.")
                        .example("JWT is missing or invalid"))
                .addProperty("path", new StringSchema()
                        .description("Request path.")
                        .example("/workouts/123"));

        return new OpenAPI()
                .info(new Info()
                        .title("FitAPI")
                        .version("v1")
                        .description("FitAPI endpoints for auth, workouts, sets, and exercises."))
                .components(new Components()
                        .addSchemas("ErrorResponse", errorSchema))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                .addServersItem(new Server()
                        .url("http://fitapi-app:8080")
                        .description("Azure"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local"))
                .tags(List.of(
                        new Tag()
                                .name("Authentication")
                                .description("User registration, login, and authenticated user info."),
                        new Tag()
                                .name("Workouts")
                                .description("Create, update, and query workout sessions. All endpoints require a valid JWT."),
                        new Tag()
                                .name("Workout Sets")
                                .description("Manage individual workout sets for sessions owned by the authenticated user."),
                        new Tag()
                                .name("Exercises")
                                .description("Read-only exercise catalog endpoints for the authenticated user."),
                        new Tag()
                                .name("Public")
                                .description("Public endpoints that do not require authentication.")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi(OpenApiCustomizer defaultErrorResponses,
                                    OperationCustomizer operationTagCustomizer) {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .pathsToExclude("/actuator/**")
                .addOpenApiCustomizer(defaultErrorResponses)
                .addOperationCustomizer(operationTagCustomizer)
                .build();
    }

    @Bean
    public OpenApiCustomizer defaultErrorResponses() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
                            return;
                        }
                        var responses = operation.getResponses();
                        addErrorIfMissing(responses, "401", "Unauthorized");
                        addErrorIfMissing(responses, "403", "Forbidden");
                        addErrorIfMissing(responses, "404", "Not found");
                    })
            );
        };
    }

    @Bean
    public OperationCustomizer operationTagCustomizer() {
        return (operation, handlerMethod) -> {
            String tag = tagForController(handlerMethod);
            if (tag != null) {
                operation.setTags(List.of(tag));
            }
            return operation;
        };
    }

    private String tagForController(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        if (WorkoutSessionController.class.isAssignableFrom(beanType)) {
            return "Workouts";
        }
        if (WorkoutSetController.class.isAssignableFrom(beanType)) {
            return "Workout Sets";
        }
        if (ExerciseController.class.isAssignableFrom(beanType)) {
            return "Exercises";
        }
        if (AuthController.class.isAssignableFrom(beanType)) {
            return "Authentication";
        }
        if (HelloController.class.isAssignableFrom(beanType)) {
            return "Public";
        }
        return null;
    }

    private void addErrorIfMissing(io.swagger.v3.oas.models.responses.ApiResponses responses,
                                   String code,
                                   String description) {
        if (responses.containsKey(code)) {
            return;
        }
        responses.addApiResponse(code, new ApiResponse()
                .description(description)
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/json",
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));
    }
}
