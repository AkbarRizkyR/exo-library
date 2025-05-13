package id.shellrean.exo.resource;


import id.shellrean.exo.dto.BookData;
import id.shellrean.exo.entity.Book;
import id.shellrean.exo.repository.BookRepository;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;




@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {

    @Inject
    BookRepository bookRepository;

    @GET
    public List<BookData> getBooks() {
        List<Book> books = bookRepository.findAllNotDeleted(); // Blocking call

        return books.stream()
                .map(book -> {
                    BookData bookData = new BookData();
                    bookData.setId(book.getId());
                    bookData.setIsbn(book.getIsbn());
                    bookData.setDescription(book.getDescription());
                    bookData.setTitle(book.getTitle());
                    return bookData;
                })
                .collect(Collectors.toList());
    }
    @POST
    @Path("/set")
    @Transactional
    public Response createOrUpdateBook(BookData bookData) {
        if ((bookData.getId() == null || bookData.getId().isEmpty()) &&
                (bookData.getTitle() == null || bookData.getTitle().isEmpty())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"ID or Title is required to create or update a book.\"}")
                    .build();
        }

        Book book = null;

        // 🔍 1. Try to find by ID first
        if (bookData.getId() != null && !bookData.getId().isEmpty()) {
            book = bookRepository.findById(bookData.getId());
        }

        // 🔍 2. If not found by ID, try to find by Title
        if (book == null && bookData.getTitle() != null && !bookData.getTitle().isEmpty()) {
            book = bookRepository.find("title", bookData.getTitle()).firstResult();
        }

        if (book == null) {
            // 🔄 3. If not found in DB, create a new Book
            book = new Book();
            book.setId(bookData.getId()); // <--- Use provided ID if it exists
            book.setCreatedAt(Instant.now());
        } else {
            // 🔄 4. If found, update timestamp
            book.setUpdatedAt(Instant.now());
        }

        // 🔄 5. Set or update the fields
        book.setIsbn(bookData.getIsbn());
        book.setTitle(bookData.getTitle());
        book.setDescription(bookData.getDescription());

        // 🔄 6. Merge instead of persist to avoid generating new ID
        bookRepository.getEntityManager().merge(book);

        // 🔄 7. Prepare the response
        BookData responseData = new BookData();
        responseData.setId(book.getId());
        responseData.setIsbn(book.getIsbn());
        responseData.setTitle(book.getTitle());
        responseData.setDescription(book.getDescription());

        return Response.ok(responseData).build();
    }

    @POST
    @Path("/delete")
    @Transactional
    public Response deleteBook(@QueryParam("id") String id) {
        try {
            if (id == null || id.isEmpty()) {
                return Response.status(400)
                        .entity("{\"error\":\"ID is required for deletion\"}")
                        .build();
            }

            boolean deleted = bookRepository.hardDelete(id);

            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(404)
                        .entity("{\"error\":\"Book not found or already deleted\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();  // <-- Log the stack trace
            return Response.status(500)
                    .entity("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}")
                    .build();
        }
    }



}