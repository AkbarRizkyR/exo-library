package id.shellrean.exo.resource;

import id.shellrean.exo.dto.LoginRequest;
import id.shellrean.exo.dto.LoginResponse;
import id.shellrean.exo.service.AuthService;
import id.shellrean.exo.dto.RegisterRequest;
import id.shellrean.exo.entity.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;


@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Email and Password must not be empty")
                    .build();
        }
        LoginResponse response = authService.authenticate(loginRequest);
        if (response != null) {
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Salah Password BRO!!!!")
                    .build();
        }
    }
    @POST
    @Path("/register")
    public Response register(RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return Response.status(Response.Status.CREATED).entity("Registrasi Berhasil").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Registration Gagal: " + e.getMessage()).build();
        }
    }
}
