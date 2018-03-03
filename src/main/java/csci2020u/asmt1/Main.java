package csci2020u.asmt1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class Main extends Application {
	
	private Label accuracyLabel;
	private Label precisionLabel;
	private TextField accuracyValue;
	private TextField precisionValue;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Prompt to locate test data first
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setInitialDirectory(new File("."));
		dirChooser.setTitle("Select directory containing test files");
		File mainDir = dirChooser.showDialog(primaryStage);

		// Application window title
		primaryStage.setTitle("Spam Detector");

		// Table
		TableView<TestFile> fileTable = new TableView<>();

		// Table Columns
		TableColumn<TestFile, String> fileNameCol = null;
		fileNameCol = new TableColumn<>("File");
		fileNameCol.setMinWidth(400);
		fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));

		TableColumn<TestFile, String> fileClassCol = null;
		fileClassCol = new TableColumn<>("Actual Class");
		fileClassCol.setMinWidth(100);
		fileClassCol.setCellValueFactory(new PropertyValueFactory<>
										("actualClass"));

		TableColumn<TestFile, Double> fileSpamProbCol = null;
		fileSpamProbCol = new TableColumn<>("Spam Probability");
		fileSpamProbCol.setMinWidth(300);
		fileSpamProbCol.setCellValueFactory(new PropertyValueFactory<>
											("spamProbability"));

		fileTable.getColumns().setAll(fileNameCol, fileClassCol, 
									  fileSpamProbCol);

		// Accuracy and Precision
		GridPane resultsSection = new GridPane();
		resultsSection.setPadding(new Insets(10, 10, 10, 10));
		accuracyLabel = new Label("Accuracy:");
		accuracyValue = new TextField();
		precisionLabel = new Label("Precision:");
		precisionValue = new TextField();
		resultsSection.add(accuracyLabel, 0, 0);
		resultsSection.add(accuracyValue, 1, 0);
		resultsSection.add(precisionLabel, 0, 1);
		resultsSection.add(precisionValue, 1, 1);

		// Set up layout of contents in application window
		BorderPane layout = new BorderPane();
		layout.setCenter(fileTable);
		layout.setBottom(resultsSection);

		// Create the application scene
		Scene scene = new Scene(layout, 800, 600);

		// Set the application scene
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
