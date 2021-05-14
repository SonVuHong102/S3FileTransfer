package main;

import java.io.File;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Sender {
	private final String bucketName;
	private final Regions region;
	private AmazonS3 s3;

	public static void main(String args[]) {
		new Sender();
	}

	public Sender() {

		// initInfomation
		bucketName = "mangmaytinhbucket1";
		region = Regions.AP_SOUTHEAST_1;

		// connect to bucket
		s3 = null;
		connectToBucketSender();
		System.out.println("Connect Successfully !");

		// get bucket objects
		List<S3ObjectSummary> listObjects = null;
		listObjects = getBucketObject();
		System.out.println("Get Object List Successfully !");

		// clear bucket
		for (S3ObjectSummary os : listObjects) {
			s3.deleteObject(bucketName, os.getKey());
		}
		System.out.println("Clear Bucket Successfully !");

		// Upload file to bucket
		File file = new File("./src/main/resources/1.png");
		s3.putObject(bucketName, file.getName(), file);
		System.out.println("Upload File Successful !");

		// Create Send Request
		String SENT_REQUEST = "UPLOAD_OK";
		s3.putObject(bucketName, SENT_REQUEST, "");
		System.out.println("Create Upload Request Successfully !");

		// Waiting for Receiver to download
		System.out.println("Waiting for Receiver to download");
		boolean isReceived = false;
		while (!isReceived) {
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
		System.out.println("Receiver has downloaded Successfully");

		// clear Bucket
		for (S3ObjectSummary os : listObjects) {
			s3.deleteObject(bucketName, os.getKey());
		}
		System.out.println("Clear Bucket Successfully !");

		// Close connection
		System.out.println("Connection Closed Successfully !");

	}

	public void connectToBucketSender() {
		s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
		if (!s3.doesBucketExistV2(bucketName)) {
			System.out.println("Bucket is NOT Exist !\nCreating Bucket");
			s3.createBucket(bucketName);
			System.out.println("Create Successful !");
		}
	}

	public List<S3ObjectSummary> getBucketObject() {
		ListObjectsV2Result list = s3.listObjectsV2(bucketName);
		return list.getObjectSummaries();
	}
}
