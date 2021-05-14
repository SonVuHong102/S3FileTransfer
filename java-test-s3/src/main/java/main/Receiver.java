package main;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Receiver {
	private String bucketName;
	private Regions region;
	private AmazonS3 s3;
	List<S3ObjectSummary> listObjects;

	public static void main(String args[])  {
		new Receiver();
	}

	public Receiver()  {

		// initInfomation
		initInformation();

		// connect to bucket
		boolean checkConnected = connectToBucket();
		if(!checkConnected)
			return;

		// Waiting for Sender to upload
		System.out.println("Waiting for Sender to upload");
		long timeLimit = 60000;
		long timeStart = System.currentTimeMillis();
		boolean isReceived = false;
		while (!isReceived && (System.currentTimeMillis()-timeStart) < timeLimit) {
			if (s3.doesObjectExist(bucketName, "UPLOAD_FAIL"))
				break;
			if (s3.doesObjectExist(bucketName, "UPLOAD_OK"))
				isReceived = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!isReceived) {
			System.out.println("Time out ! Sender has fail to upload on time !");
			return;
		}
		System.out.println("Sender has uploaded Successfully !");
		
		// get bucket objects
		getBucketObject();

		// Download Files
		for (S3ObjectSummary os : listObjects) {
			if (os.getKey().equals((String) "UPLOAD_OK"))
				continue;
			try {
				S3Object o = s3.getObject(bucketName, os.getKey());
				S3ObjectInputStream oinp = o.getObjectContent();
				FileOutputStream fos = new FileOutputStream(new File("D:\\" + os.getKey()));
				byte[] read_buf = new byte[1024];
				int read_len = 0;
				while ((read_len = oinp.read(read_buf)) > 0) {
					fos.write(read_buf, 0, read_len);
				}
				oinp.close();
				fos.close();
			} catch (Exception e) {
				System.out.println("Error Downloading File !");
				e.printStackTrace();
				return;
			}
		}
		System.out.println("File downloaded Successfully ! Saved at D:\\");

		// Create Receiver Confirmation
		String RECEIVER_CONFIRMATION = "RECEIVE_OK";
		s3.putObject(bucketName, RECEIVER_CONFIRMATION, "");
		System.out.println("Create Receiver Confirmation Successfully !");

		// close connection
		System.out.println("Connection Closed Successfully !");
	}
	
	public void initInformation() {
		Random r = new Random();
		System.out.println("Enter code from Sender : ");
		Scanner inp = new Scanner(System.in);
		int code = inp.nextInt();
		bucketName = "mangmaytinhbucket" + code;
		region = Regions.AP_SOUTHEAST_1;
	}

	public boolean connectToBucket() {
		s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
		if (!s3.doesBucketExistV2(bucketName)) {
			System.out.println("Bucket is NOT Exist !\nTry another code !");
			return false;
		}
		s3.putObject(bucketName, "ReceiverConnected", "");
		System.out.println("Connected to Bucket !");
		return true;
	}

	public void getBucketObject() {
		ListObjectsV2Result list = s3.listObjectsV2(bucketName);
		listObjects = list.getObjectSummaries();
		System.out.println("Get Object List Successfully !");
	}
}
