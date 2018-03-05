package csci2020u.asmt1;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Comparable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;


public class Main extends Application {

	private final static int WIN_WIDTH = 800;
	private final static int WIN_HEIGHT = 600;
	private final static String[] FILE_TYPE = { "Ham", "Spam" };
	private ArrayList<String> wordList;
	private TreeMap<String, Integer> trainingHamFreq;
	private TreeMap<String, Integer> trainingSpamFreq;
	private TreeMap<String, Double> probabilityOfSpamIfContains;
	private ObservableList<TestFile> testResults;
	private double accuracy;
	private double precision;
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
		dirChooser.setTitle("Select directory containing test files");
		File testFilesDir = dirChooser.showDialog(primaryStage);

		try {
			// Throw error if user cancelled during directory selecting prompt
			if (trainingFilesDir == null || testFilesDir == null) {
				throw new FileNotFoundException("No directory selected");
			}
			// Load training data and train program
			trainProgram(trainingFilesDir);

			// Load test data and attempt to classify test files
			testResults = testFiles(testFilesDir);
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
			//e.printStackTrace();
		}

		// Table
		TableView<TestFile> fileTable = new TableView<>();
		fileTable.setItems(testResults);
		fileTable.setEditable(false);

		// Table Columns
		TableColumn<TestFile, String> fileNameCol = null;
		fileNameCol = new TableColumn<>("File");
		fileNameCol.setMinWidth(Math.round(WIN_WIDTH*0.5625));
		fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));

		TableColumn<TestFile, String> fileClassCol = null;
		fileClassCol = new TableColumn<>("Actual Class");
		fileClassCol.setMinWidth(Math.round(WIN_WIDTH*0.125));
		fileClassCol.setCellValueFactory(new PropertyValueFactory<>("actualClass"));

		TableColumn<TestFile, String> fileSpamProbCol = null;
		fileSpamProbCol = new TableColumn<>("Spam Probability");
		fileSpamProbCol.setMinWidth(Math.round(WIN_WIDTH*0.3125));
		fileSpamProbCol.setCellValueFactory(new PropertyValueFactory<>("roundedSpamProbability"));

		fileTable.getColumns().setAll(fileNameCol, fileClassCol, fileSpamProbCol);

		// Accuracy and Precision
		GridPane resultsSection = new GridPane();
		resultsSection.setPadding(new Insets(10, 10, 10, 10));
		accuracyLabel = new Label("Accuracy:\t");
		accuracyValue = new TextField(new DecimalFormat("0.00000").format(accuracy));
		accuracyValue.setEditable(false);
		precisionLabel = new Label("Precision:\t");
		precisionValue = new TextField(new DecimalFormat("0.00000").format(precision));
		precisionValue.setEditable(false);
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
	 * Recursively iterates through all files and subdirectories
	 * and adds ham and spam files to their respective lists
	 *
	 *
	 * @param fileDir The directory to begin in
	 * @param type Currently iterating over ham or spam files
	 * @param hamFileList The list of ham files to add to
	 * @param spamFileList The list of spam files to add to
	 *
	 * @exception IOException if an I/O error occurs while reading a file
	 * /sub-directory
	 */
	private void buildFileList(File fileDir, String type, ArrayList<File> hamFileList, ArrayList<File> spamFileList) throws IOException {

		// Check directory permissions and whether it exists or not
		if (fileDir.exists() && fileDir.canRead()) {

			// Read contents of fileDir
			for (File file: fileDir.listFiles()) {

				if (file.isDirectory()) {
					// Sub-directory: Recurse over it
					if (file.getName().toLowerCase().contains(FILE_TYPE[0].toLowerCase())) {
						buildFileList(file, type, hamFileList, spamFileList);
					} else {
						buildFileList(file, FILE_TYPE[1], hamFileList, spamFileList);
					}
				} else {
					// File: Add to list
					if (type == FILE_TYPE[0]) {
						hamFileList.add(file);
					} else {
						spamFileList.add(file);
					}
				}
			}
		}
	}

	/**
	 * Reads contents of the file and count the number of times each word 
	 * appears in the file
	 *
	 *
	 * @param file The file to read
	 *
	 * @return The TreeMap<String, Integer> containing the list of words and 
	 * the number of times they appear in the file
	 *
	 * @exception FileNotFoundException if the file does not exist
	 */
	private TreeMap<String, Integer> parseFile(File file) throws FileNotFoundException {

		TreeMap<String, Integer> wordCount = new TreeMap<>();
		String pattern = "^[a-zA-z]+$";
		Scanner scanner = new Scanner(file);
		scanner.useDelimiter("[\\s\\.:;\\?\\!,]");

		// Read contents of file
		while (scanner.hasNext()) {
			String word = scanner.next().toLowerCase();

			if (word.matches(pattern)) {
				incrementCount(word, wordCount);
			}
		}


		return wordCount;

	}

	/**
	 * Reads contents of the file, counts the number of times each word 
	 * appears in the file and adds newly encountered words to the dictionary 
	 * of total words encountered overall
	 *
	 *
	 * @param file The file to read
	 * @param numFilesContainingWord The TreeMap<String, Integer> containing 
	 * the number of ham/spam files that contains words that the file contains
	 *
	 * @exception FileNotFoundException if the file does not exist
	 * @exception IOException if an I/O error occurs while reading the file
	 */
	private void parseTrainingFile(File file, TreeMap<String, Integer> numFilesContainingWord) throws FileNotFoundException, IOException {

		// Read file contents and get its list of words
		TreeMap<String, Integer> wordCount = parseFile(file);

		// Add to the count of files containing the words in file
		for (String word: wordCount.keySet()) {
			incrementCount(word, numFilesContainingWord);
			// Add to list of all words encountered if not done so already
			if (!wordList.contains(word)) {
				wordList.add(word);
			}
		}
	}

	/**
	 * Reads contents of the file, counts the number of times each word 
	 * appears in the file and determines, from the training probability
	 * data, how likely the file is to be spam
	 *
	 *
	 * @param file The file to read
	 * @param type Whether the input file is "Ham" or "Spam"
	 *
	 * @return a TestFile object containing the file's name, spam probabiltiy 
	 * and its actual classification
	 *
	 * @exception FileNotFoundException if the file does not exist
	 * @exception IOException if an I/O error occurs while reading the file
	 */
	private TestFile parseTestFile(File file, String type) throws FileNotFoundException, IOException {
		
		// Read file contents and get its list of words
		TreeMap<String, Integer> wordCount = parseFile(file);
		double spamProbability = 0.0;
		double wordSpamProbability = 0.0;
		double totalWordSpamProb = 0.0;

		// Loop over the different words in file
		for (String word: wordCount.keySet()) {
			// Get probability of word
			if (probabilityOfSpamIfContains.containsKey(word)) {
				wordSpamProbability = probabilityOfSpamIfContains.get(word);
			} else {
				wordSpamProbability = 0.0;
			}
			totalWordSpamProb += wordCount.get(word) * (Math.log(1-wordSpamProbability) - Math.log(wordSpamProbability));
		}

		// Calculate spam probability of file
		if (Double.isNaN(totalWordSpamProb)) {
			// Check for Not-a-Number
			spamProbability = 1.0;
		} else if (totalWordSpamProb == Double.POSITIVE_INFINITY) {
			// Check for positive infinity
			spamProbability = 0.0;
		} else if (totalWordSpamProb == Double.NEGATIVE_INFINITY) {
			// Check for negative infinity
			spamProbability = 1.0;
		} else {
			spamProbability = 1.0 / (1.0+Math.exp(totalWordSpamProb));
		}
		
		return new TestFile(file.getName(), spamProbability, type);
	}

	/**
	 * Increments the count of a word in a TreeMap<String, Integer> object
	 *
	 *
	 * @param word The word to increment the count of
	 * @param map The TreeMap<String, Integer> that keeps the count of words
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
	 * Reads the training files to map spam probabilities to individual words 
	 * based on their appearance rate in ham/spam files
	 *
	 *
	 * @param dir The directory of the training data
	 *
	 * @exception FileNotFoundException if a file was not found
	 * @exception IOException if an error occurs while 
	 */
	private void trainProgram(File dir) throws FileNotFoundException, IOException {

		ArrayList<File> hamFileList = new ArrayList<>();
		ArrayList<File> spamFileList = new ArrayList<>();
		wordList = new ArrayList<>();
		trainingHamFreq = new TreeMap<>();
		trainingSpamFreq = new TreeMap<>();
		probabilityOfSpamIfContains = new TreeMap<>();

		// Build list of ham and spam files
		buildFileList(dir, FILE_TYPE[0], hamFileList, spamFileList);

		// Parse ham files
		for (File file: hamFileList) {
			parseTrainingFile(file, trainingHamFreq);
		}
		// Parse spam files
		for (File file: spamFileList) {
			parseTrainingFile(file, trainingSpamFreq);
		}

		String word = "";
		Iterator<String> wordListIter = wordList.iterator();
		double wordCountInHam = 0.0;
		double wordCountInSpam = 0.0;
		double hamAppearProbability = 0.0;
		double spamAppearProbability = 0.0;
		double probability = 0.0;
		int hamListSize = hamFileList.size();
		int spamListSize = spamFileList.size();

		// Check all words that appeared in training files
		while (wordListIter.hasNext()) {
			word = wordListIter.next();

			if (trainingHamFreq.containsKey(word)) {
				// Word shows up in ham files
				wordCountInHam = trainingHamFreq.get(word);
			} else {
				// Word does not show up in ham files
				wordCountInHam = 0.0;
			}
			if (trainingSpamFreq.containsKey(word)) {
				// Word shows up in spam files as well
				wordCountInSpam = trainingSpamFreq.get(word);
			} else {
				// Word does not show up in spam files at all
				wordCountInSpam = 0.0;
			}
			
			// Calculate probability of file being spam if it contains a specific word
			hamAppearProbability = wordCountInHam / hamListSize;
			spamAppearProbability = wordCountInSpam / spamListSize;
			if ((spamAppearProbability + hamAppearProbability) == 0.0) {
				// Handle division by 0
				probability = 0.0;
			} else {
				probability = spamAppearProbability / (spamAppearProbability + hamAppearProbability);
			}
			probabilityOfSpamIfContains.put(word, probability);

			
		}
	}

	/**
	 * Uses the accumulated training data to attempt to classify test data
	 * as either ham or spam emails based on the probability of word appearances
	 *
	 *
	 * @param dir The directory containing the test files
	 *
	 * @return an ObservableList of TestFile objects representing the result
	 * of a tested file 
	 *
	 * @exception FileNotFoundException if a file was not found
	 * @exception IOException if an error occurs while reading the test files
	 */
	private ObservableList<TestFile> testFiles(File dir) throws FileNotFoundException, IOException {

		ArrayList<File> hamFileList = new ArrayList<>();
		ArrayList<File> spamFileList = new ArrayList<>();
		ObservableList<TestFile> testFileResults = FXCollections.observableArrayList();

		// Build list of ham and spam files
		buildFileList(dir, FILE_TYPE[0], hamFileList, spamFileList);

		// Sort file lists by Name
		hamFileList.sort(null);
		spamFileList.sort(null);

		// Parse ham files
		for (File file: hamFileList) {
			testFileResults.add(parseTestFile(file, FILE_TYPE[0]));
		}
		// Parse spam files
		for (File file: spamFileList) {
			testFileResults.add(parseTestFile(file, FILE_TYPE[1]));
		}

		// Calculate accuracy and precision of test results
		double numTruePositives = 0.0;
		double numTrueNegatives = 0.0;
		double numFalsePositives = 0.0;
		TestFile file = null;
		Iterator<TestFile> testFileIter = testFileResults.iterator();

		while (testFileIter.hasNext()) {
			file = testFileIter.next();

			if (file.getActualClass().equals("Spam") && file.getSpamProbability() > 0.5) {
				numTruePositives++;
			} else if (file.getActualClass().equals("Ham") && file.getSpamProbability() <= 0.5) {
				numTrueNegatives++;
			} else if (file.getActualClass().equals("Ham") && file.getSpamProbability() > 0.5) {
				numFalsePositives++;
			}
		}
		accuracy =  (numTruePositives + numTrueNegatives) / (hamFileList.size() + spamFileList.size());
		precision = numTruePositives / (numFalsePositives + numTruePositives);

		return testFileResults;
	}

	/**
	 * Launch the JavaFX application
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
