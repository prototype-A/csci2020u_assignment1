package csci2020u.asmt1;

import java.text.DecimalFormat;


public class TestFile {

	private String fileName;
	private double spamProbability;
	private String actualClass;

	public TestFile(String fileName, double spamProbability, 
					String actualClass) {
		this.fileName = fileName;
		this.spamProbability = spamProbability;
		this.actualClass = actualClass;
	}

	public String getFileName() {
		return fileName;
	}

	public double getSpamProbability() {
		return spamProbability;
	}

	public String getRoundedSpamProbability() {
		return new DecimalFormat("0.00000").format(spamProbability);
	}

	public String getActualClass() {
		return actualClass;
	}

	public void setFileName(String newFileName) {
		fileName = newFileName;
	}

	public void setSpamProbability(double probability) {
		spamProbability = probability;
	}

	public void setActualClass(String actualClass) {
		this.actualClass = actualClass;
	}

}
