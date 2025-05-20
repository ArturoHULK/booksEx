package com.jarmdev.books.controller;

import com.jarmdev.books.entity.Book;
import com.jarmdev.books.exception.BookNotFoundException;
import com.jarmdev.books.request.BookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books REST API endpoint", description = "operations related to books")
public class BooksController {

    private final List<Book> books =  new ArrayList<>();

    public BooksController() {
        loadBooks();
    }

    private void loadBooks() {
        books.addAll(List.of(
                new Book(1L,"Computer Science Pro","Chad Darby","Computer Science", 5),
                new Book(2L,"Java Spring Master","Eric Roby","Computer Science", 5),
                new Book(3L,"Why 1+1 rocks","Adil A.","Math", 5),
                new Book(4L,"How Bears Hibernate","Bob B.","Science", 2),
                new Book(5L,"A pirate's treasure","Curt C.","History", 3),
                new Book(6L,"Why 2+2 is better","Dan D.","Math", 5)
        ));
    }

    //query parameter = request param
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all books", description = "Retrieve a list of all available books")
    public List<Book> allBooks(@Parameter(description = "Optional query parameter")
                                   @RequestParam(required = false) String category) {
        if (category != null) {
            return books.stream()
                    .filter(b -> b.getCategory().equals(category))
                    .toList();
        }
        return books;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a book by Id", description = "Retrieve a specific book by its Id")
    public Book findBookById(@Parameter(description = "Id of the book to retrieve")
                                 @PathVariable @Min(value = 1) Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElseThrow(()-> new BookNotFoundException("Book not found - " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new book", description = "Add a new book to the list")
    public Book createBook(@RequestBody @Valid BookRequest bookRequest) {
        Long id = books.isEmpty() ? 1L : books.get(books.size()-1).getId() + 1;

        //if we were using @services this method should be there
        Book newBook = convertToBook(id, bookRequest);
        books.add(newBook);

        return newBook;
    }

    private Book convertToBook(@Min(value = 1) Long id, @Valid BookRequest bookRequest) {
        return new Book(id, bookRequest.getTitle(), bookRequest.getAuthor(), bookRequest.getCategory(), bookRequest.getRating());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update a book", description = "Update the details of an existing book")
    public Book updateBook(@RequestBody @Valid BookRequest bookRequest, @Parameter(description = "Id of the book to update")
        @PathVariable @Min(value = 1) Long id) {
        //in this case the for loop is better than using streams
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(id)) {
                Book book = convertToBook(id, bookRequest);
                books.set(i, book);
                return book;
            }
        }
        throw new BookNotFoundException("Book not found - " + id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book", description = "Remove a book from the list")
    public void deleteBook(@Parameter(description = "Id of the book to delete")
                               @PathVariable Long id) {
        books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElseThrow(()-> new BookNotFoundException("Book not found - " + id));

        books.removeIf(book -> book.getId().equals(id));
    }


}
