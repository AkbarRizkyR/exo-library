package id.shellrean.exo.repository;


import id.shellrean.exo.entity.Book;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;


@ApplicationScoped
public class BookRepository implements PanacheRepository<Book> {
    public List<Book> findAllNotDeleted() {
        return list("deletedAt is null"); // Blocking call
    }
    public Book findById(String id) {  // Change parameter from Long to String
        return find("id", id).firstResult();
    }
    @Transactional
    public boolean hardDelete(String id) {
        return delete("id", id) > 0;
    }
}
