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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;


public class Main extends Application {

	private static enum FileType { HAM, SPAM }
	private TreeMap<String, Integer> trainingHamFreq;
	private TreeMap<String, Integer> trainingSpamFreq;
	private TreeMap<String, Double> probabilityOfSpamIfContains;
	private Label accuracyLabel;
	private Label precisionLabel;
	private TextField accuracyValue;
	private TextField precisionValue;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Prompt user to locate training and test data
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setInitialDirectory(new File("."));
		dirChooser.setTitle("Select directory containing training files");
		File trainingFilesDir = dirChooser.showDialog(primaryStage);
		//dirChooser.setTitle("Select directory containing test files");
		//File testFilesDir = dirChooser.showDialog(primaryStage);

		try {
			// Load training data and train program
			trainProgram(trainingFilesDir);

		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
			e.printStackTrace();
		}

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

		// Application window title
		primaryStage.setTitle("Spam Detector");

		// Create the application scene
		Scene scene = new Scene(layout, 800, 600);

		// Set the application scene
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 *	Recursively iterates through all training files and subdirectories
	 *	and adds ham and spam files to their respective lists
	 *
	 *	@param fileDir The directory to begin in
	 *	@param type Currently iterating over ham or spam files
	 *	@param hamFileList The list of ham files to add to
	 *	@param spamFileList The list of spam files to add to
	 *
	 *	@exception IOException if an I/O error occurs while reading a file
	 *	/sub-directory
	 */
	private void buildFileList(File fileDir, FileType type, 
								ArrayList<File> hamFileList, 
								ArrayList<File> spamFileList)
											throws IOException {

		// Check directory permissions and whether it exists or not
		if (fileDir.exists() && fileDir.canRead()) {

			// Read contents of fileDir
			for (File file: fileDir.listFiles()) {

				if (file.isDirectory()) {
					// Sub-directory: Recurse over it
					if (file.getName().toLowerCase().contains("ham")) {
						buildFileList(file, type, hamFileList, spamFileList);
					} else if (file.getName().toLowerCase().contains("spam")) {
						buildFileList(file, FileType.SPAM, 
										hamFileList, spamFileList);
					}
				} else {
					// File: Add to list
					if (type == FileType.HAM) {
						hamFileList.add(file);
					} else {
						spamFileList.add(file);
					}
				}
			}
		}
	}

	/**
	 *	Reads the training files to map spam probabilities to individual words 
	 *	based on their appearance rate in ham/spam files
	 *
	 *	@param dir The directory of the training data
	 *
	 *	@exception IOException if an error occurs while 
	 */
	private void trainProgram(File dir) throws FileNotFoundException,
														 IOException {

		ArrayList<File> hamFileList = new ArrayList<>();
		ArrayList<File> spamFileList = new ArrayList<>();
		trainingHamFreq = new TreeMap<>();
		trainingSpamFreq = new TreeMap<>();
		probabilityOfSpamIfContains = new TreeMap<>();

		// Build list of ham and spam files
		buildFileList(dir, FileType.HAM, hamFileList, spamFileList);

		// Parse ham files
		for (File file: hamFileList) {
			parseFile(file, trainingHamFreq);
		}
		// Parse spam files
		for (File file: spamFileList) {
			parseFile(file, trainingSpamFreq);
		}

		// Calculate probability of file being spam if it contains
		// a specific word
		for (String word: trainingSpamFreq.keySet()) {
			double wordCountInHam = 0.0;
			if (trainingHamFreq.containsKey(word)) {
				wordCountInHam = trainingHamFreq.get(word);
			}
			double wordCountInSpam = 0.0;
			if (trainingSpamFreq.containsKey(word)) {
				wordCountInSpam = trainingSpamFreq.get(word);
			}
			double probability = wordCountInSpam / 
								(wordCountInSpam + wordCountInHam);
			System.out.println(word + ": " + probability);
			probabilityOfSpamIfContains.put(word, probability);
		}
	}

	/**
	 *	Reads contents of file, count the number of each word in the file and
	 *	
	 *
	 *	@param file The file to read
	 *	@param numFilesContainingWord The TreeMap<String, Integer> containing 
	 *	the number of ham/spam files that contains words that the file contains
	 *
	 *	@exception FileNotFoundException if the file does not exist
	 */
	private void parseFile(File file, 
							TreeMap<String, Integer>numFilesContainingWord) 
												throws FileNotFoundException {

		TreeMap<String, Integer> wordCount = new TreeMap<>();
		String pattern = "^[a-zA-z]+$";
		Scanner scanner = new Scanner(file);
		scanner.useDelimiter("[\\s\\.:;\\?\\!,]");
		int numWords = 0;

		// Read contents of file
		while (scanner.hasNext()) {
			String word = scanner.next().toLowerCase();

			if (word.matches(pattern)) {
				incrementCount(word, wordCount);
				numWords++;
			}
		}

		// Add to the count of files containing the words in file
		for (String word: wordCount.keySet()) {
			incrementCount(word, numFilesContainingWord);
		}

	}

	/**
	 *	Increments the count of a word in a TreeMap<String, Integer> object
	 *
	 *	@param word The word to increment the count of
	 *	@param map The TreeMap<String, Integer> that keeps the count of words
	 */
	private void incrementCount(String word, TreeMap<String, Integer> map) {
		if (map.containsKey(word)) {
			// Map already contains word and its count: Increment the count by 1
			map.put(word, map.get(word) + 1);
		} else {
			// Add new word to map with its count at 1
			map.put(word, 1);
		}
	}

	/**
	 *	Launches the JavaFX application
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
