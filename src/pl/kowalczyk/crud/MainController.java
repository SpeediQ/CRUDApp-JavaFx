package pl.kowalczyk.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    private Label label;

    @FXML
    private TextField tfId;

    @FXML
    private TextField tfTitle;

    @FXML
    private TextField tfAuthor;

    @FXML
    private TextField tfYear;

    @FXML
    private TextField tfPages;

    @FXML
    private TableView<Books> tvBooks;

    @FXML
    private TableColumn<Books, Integer> colId;

    @FXML
    private TableColumn<Books, String> colTitle;

    @FXML
    private TableColumn<Books, String> colAuthor;

    @FXML
    private TableColumn<Books, Integer> colYear;

    @FXML
    private TableColumn<Books, Integer> colPages;

    @FXML
    private Button btnInsert;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnClearLogs;

    @FXML
    private Button btnGenerate;

    @FXML
    private Button btnDeleteAllBooks;

    @FXML
    private ListView<String> lstLogs;

    private static String currentId = "";

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (event.getSource() == btnInsert) {
            if (areThereEmptyFields()) {
                Alert alert = new Alert(Alert.AlertType.NONE, "Complete the fields.", ButtonType.OK);
                alert.showAndWait();
            } else if (!isBookExists(tfId.getText()) && !isTitleExists(tfTitle.getText())) {
                insertRecord();
            } else if (!isBookExists(tfId.getText()) && isTitleExists(tfTitle.getText())) {
                Alert alert = new Alert(Alert.AlertType.NONE, "The [" + tfTitle.getText() + "] already exists. Are you sure?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    insertRecord();
                }
            } else if (isBookExists(tfId.getText())) {
                Alert alert = new Alert(Alert.AlertType.NONE, "ID is already taken. Do you want to replace the book?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    updateRecord();
                }
            }


        } else if (event.getSource() == btnUpdate) {
            if (areThereEmptyFields()) {
                Alert alert = new Alert(Alert.AlertType.NONE, "No book selected.", ButtonType.OK);
                alert.showAndWait();
            } else if (currentId.equals(tfId.getText())) {
                updateRecord();
            } else {
                Alert alert = new Alert(Alert.AlertType.NONE, "The ID is assigned to another book.", ButtonType.OK);
                alert.showAndWait();

            }


        } else if (event.getSource() == btnDelete) {
            if (areThereEmptyFields()) {
                Alert alert = new Alert(Alert.AlertType.NONE, "No book selected.", ButtonType.OK);
                alert.showAndWait();
            } else if (currentId.equals(tfId.getText())) {
                Alert alert = showAlertWindow("DELETE BOOK");
                if (alert.getResult() == ButtonType.YES) {
                    deleteButton();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.NONE, "The ID is assigned to another book.", ButtonType.OK);
                alert.showAndWait();

            }


        } else if (event.getSource() == btnClear) {
            clearTextFields();
        } else if (event.getSource() == btnGenerate) {

            String firstEmptyId = findFirstEmptyId();
            tfId.setText(firstEmptyId);
        } else if (event.getSource() == btnClearLogs) {
            lstLogs.getItems().clear();
        }else if (event.getSource() == btnDeleteAllBooks) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Are you sure you want to delete all books from the database?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                deleteAllBooks();
            }
        }

    }

    private void clearTextFields() {
        tfId.clear();
        tfTitle.clear();
        tfAuthor.clear();
        tfYear.clear();
        tfPages.clear();
    }

    @FXML
    private void handleMouseAction(MouseEvent event) {
        try {
            Books book = tvBooks.getSelectionModel().getSelectedItem();
            tfId.setText(String.valueOf(book.getId()));
            tfTitle.setText(book.getTitle());
            tfAuthor.setText(book.getAuthor());
            tfYear.setText(String.valueOf(book.getYear()));
            tfPages.setText(String.valueOf(book.getPages()));

            currentId = tfId.getText();

        } catch (Exception ex) {
            System.out.println("Nothing marked");

        }


    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showBooks();
    }

    public Connection getConnection() {
        Connection conn;
        String url = "jdbc:mysql://localhost:3306/library?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user = "root";
        String password = "password";
        try {
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
    }


    public ObservableList<Books> getBooksList() {
        ObservableList<Books> bookList = FXCollections.observableArrayList();
        Connection conn = getConnection();
        String query = "SELECT * FROM books";
        Statement statement;
        ResultSet resultSet;

        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(query);
            Books books;
            while (resultSet.next()) {

                books = new Books(resultSet.getInt("id"), resultSet.getString("title"), resultSet.getString("author"), resultSet.getInt("year"), resultSet.getInt("pages"));
                bookList.add(books);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bookList;
    }

    public void showBooks() {
        ObservableList<Books> list = getBooksList();

        colId.setCellValueFactory(new PropertyValueFactory<Books, Integer>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<Books, String>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<Books, String>("author"));
        colYear.setCellValueFactory(new PropertyValueFactory<Books, Integer>("year"));
        colPages.setCellValueFactory(new PropertyValueFactory<Books, Integer>("pages"));

        tvBooks.setItems(list);
    }

    private void insertRecord() {
        String id = tfId.getText();
        String title = tfTitle.getText();
        String query = "INSERT INTO books VALUES (" + id + ",'" + title + "','" + tfAuthor.getText() + "',"
                + tfYear.getText() + "," + tfPages.getText() + ")";

        executeQuery(query);
        showBooks();
        LocalDateTime localDateTime = LocalDateTime.now();
        addNewLog("Book added [ ID = " + tfId.getText() + " ]. - " + convertToPrettyString(localDateTime));
        clearTextFields();

    }

    private void updateRecord() {
        String query = "UPDATE books SET title  = '" + tfTitle.getText() + "', author = '" + tfAuthor.getText() + "', year = " +
                tfYear.getText() + ", pages = " + tfPages.getText() + " WHERE id = " + tfId.getText() + "";
        executeQuery(query);
        showBooks();
        LocalDateTime localDateTime = LocalDateTime.now();
        addNewLog("Book updated [ ID = " + tfId.getText() + " ]. - " + convertToPrettyString(localDateTime));
        clearTextFields();
    }

    private void deleteButton() {
        String query = "DELETE FROM books WHERE id =" + tfId.getText() + "";
        executeQuery(query);
        showBooks();
        LocalDateTime localDateTime = LocalDateTime.now();
        addNewLog("Book deleted [ ID = " + tfId.getText() + " ]. - " + convertToPrettyString(localDateTime));
        clearTextFields();

    }

    private void deleteAllBooks() {
        String query = "DELETE FROM books;";
        executeQuery(query);
        showBooks();
        LocalDateTime localDateTime = LocalDateTime.now();
        addNewLog("Books have been removed from the database. - " + convertToPrettyString(localDateTime));
        clearTextFields();

    }

    private void executeQuery(String query) {
        Connection conn = getConnection();
        Statement st;
        try {
            st = conn.createStatement();
            st.executeUpdate(query);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String findFirstEmptyId() {
        int intId = 1;
        while (isBookExists("" + intId)) {
            intId++;
        }
        return "" + intId;
    }

    private boolean isBookExists(String id) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM books WHERE id = ?");
            preparedStatement.setString(1, id);
            ResultSet my_rs = preparedStatement.executeQuery();

            if (my_rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean isTitleExists(String title) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM books WHERE title = ?");
            preparedStatement.setString(1, title);
            ResultSet my_rs = preparedStatement.executeQuery();

            if (my_rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Alert showAlertWindow(String alertTitle) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Are you sure?", ButtonType.YES, ButtonType.NO);
        alert.setTitle(alertTitle);
        alert.showAndWait();

        return alert;
    }

    private void addNewLog(String log) {
        lstLogs.getItems().add(log);
    }


    private String convertToPrettyString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    private boolean areThereEmptyFields() {
        String id = tfId.getText();
        String title = tfTitle.getText();
        String author = tfAuthor.getText();
        String year = tfYear.getText();
        String pages = tfPages.getText();

        return (id.equals("")||title.equals("")||author.equals("")||year.equals("")||pages.equals(""));
    }



}
