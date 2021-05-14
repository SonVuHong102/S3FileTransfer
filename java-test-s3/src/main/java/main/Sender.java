package main;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Sender {
	private String bucketName;
	private Regions region;
	private AmazonS3 s3;
	List<S3ObjectSummary> listObjects;
	private int code;
	private long sizeLimit = 26214400; //25MB

	public static void main(String args[]) {
		new Sender();
	}

	public Sender() {

		// initInfomation
		initInformation();

		// connect to bucket
		connectToBucket();

		// get Receiver connection status
		boolean connected = checkConnected();

		// get bucket objects
		getBucketObject();

		// clear bucket
		clearBucket();
		
		//delete Bucket if Receiver is not connected
		if(!connected) {
			System.out.println("Time out ! Receiver is not connected !");
			deleteBucket();
			return;
		} else {
			System.out.println("Receiver is connected !");
		}

		// Upload file to bucket
		uploadFile();

		// Waiting for Receiver to download
		System.out.println("Waiting for Receiver to download...");
		boolean isReceived = false;
		long timeLimit = 60000;
		long timeStart = System.currentTimeMillis();
		while (!isReceived && (System.currentTimeMillis()-timeStart) < timeLimit ) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (s3.doesObjectExist(bucketName, "RECEIVE_OK")) {
				isReceived = true;
			}
		}
		if(isReceived)
			System.out.println("Receiver has downloaded Successfully !");
		else
			System.out.println("Receiver has NOT downloaded files !");
		
		// get bucket objects
		getBucketObject();

		// clear Bucket
		clearBucket();

		// delete Bucket
		deleteBucket();

		// Close connection
		System.out.println("Connection Closed Successfully !");

	}

	public void initInformation() {
		Random r = new Random();
		code = Math.abs(r.nextInt()) % 1000;
		bucketName = "mangmaytinhbucket" + code;
		region = Regions.AP_SOUTHEAST_1;
	}

	public void connectToBucket() {
		s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
		if (!s3.doesBucketExistV2(bucketName)) {
			System.out.println("Bucket is available !\nCreating Bucket...");
			s3.createBucket(bucketName);
			System.out.println("Create Successful !");
		} else {
			System.out.println("Bucket is in using !\nChanging Code...");
			initInformation();
		}
		System.out.println("Connect Successfully !");
		System.out.println("Send this code for Receiver : " + code);
	}

	public boolean checkConnected() {
		s3.putObject(bucketName, "SenderConnected", "");
		boolean isReceiverConnected = false;
		long timeLimit = 30000;
		long timeStart = System.currentTimeMillis();
		while (!isReceiverConnected && (System.currentTimeMillis()-timeStart) < timeLimit ) {
			if (s3.doesObjectExist(bucketName, "ReceiverConnected"))
				isReceiverConnected = true;
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return isReceiverConnected;
	}

	public void getBucketObject() {
		ListObjectsV2Result list = s3.listObjectsV2(bucketName);
		listObjects = list.getObjectSummaries();
		System.out.println("Get Object List Successfully !");
	}
	
	public void clearBucket() {
		for (S3ObjectSummary os : listObjects) {
			s3.deleteObject(bucketName, os.getKey());
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Clear Bucket Successfully !");
	}
	
	public void deleteBucket() {
		s3.deleteBucket(bucketName);
		System.out.println("Delete Bucket Successfully !");
	}
	
	public void  uploadFile() {
		Scanner inp = new Scanner(System.in);
		System.out.println("Enter File Path : ");
		String path = inp.nextLine();
//		File file = new File("./src/main/resources/1.png");
		File file = new File(path);
		if(file.length() > sizeLimit) {
			
			System.out.println("File is too big !\nTry another file");
			s3.putObject(bucketName, "UPLOAD_FAIL", "");
			System.out.println("Delete Bucket in 5 seconds !");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getBucketObject();
			clearBucket();
			deleteBucket();
			System.exit(1);
		}
		s3.putObject(bucketName, file.getName(), file);
		System.out.println("Upload File Successful !");
		String SENT_REQUEST = "UPLOAD_OK";
		s3.putObject(bucketName, SENT_REQUEST, "");
		System.out.println("Create Upload Request Successfully !");
	}
}
