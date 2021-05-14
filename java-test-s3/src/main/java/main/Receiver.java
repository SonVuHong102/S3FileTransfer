package main;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Receiver {
	private final String bucketName;
	private final Regions region;
	private AmazonS3 s3;
	private boolean bucketIsExist = false;

	public static void main(String args[]) throws InterruptedException {
		new Receiver();
	}

	public Receiver() throws InterruptedException {

		// initInfomation
		bucketName = "mangmaytinhbucket1";
		region = Regions.AP_SOUTHEAST_1;

		// connect to bucket
		s3 = null;
		connectToBucketReceiver();
		if (!bucketIsExist)
			return;
		System.out.println("Connect Successfully !");

		// get bucket objects
		List<S3ObjectSummary> listObjects = null;
		listObjects = getBucketObject();
		System.out.println("Get Object List Successfully !");

		// Waiting for Sender to upload
		System.out.println("Waiting for Sender to upload");
		boolean isReceived = false;
		while (!isReceived) {
			if (s3.doesObjectExist(bucketName, "UPLOAD_OK"))
				isReceived = true;
			Thread.sleep(3000);
		}

		// Download Files
		for (S3ObjectSummary os : listObjects) {
			if (os.getKey().equals((String) "UPLOAD_OK"))
				continue;
			try {
				S3Object o = s3.getObject(bucketName, os.getKey());
				S3ObjectInputStream oinp = o.getObjectContent();
				FileOutputStream fos = new FileOutputStream(new File("D:\\ReceivedFiles\\" + os.getKey()));
				byte[] read_buf = new byte[1024];
				int read_len = 0;
				while ((read_len = oinp.read(read_buf)) > 0) {
					fos.write(read_buf, 0, read_len);
				}
				oinp.close();
				fos.close();
			} catch (Exception e) {
				System.out.println("Error Downloading File !");
				return;
			}
		}

		// Create Receiver Confirmation
		String RECEIVER_CONFIRMATION = "RECEIVE_OK";
		s3.putObject(bucketName, RECEIVER_CONFIRMATION, "");
		System.out.println("Create Receiver Confirmation Successfully !");

		// close connection
		System.out.println("Connection Closed Successfully !");
	}

	public void connectToBucketReceiver() {
		s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
		if (!s3.doesBucketExistV2(bucketName)) {
			System.out.println("Bucket is NOT Exist !\nTry another name !");
			return;
		}
		bucketIsExist = true;
	}

	public List<S3ObjectSummary> getBucketObject() {
		ListObjectsV2Result list = s3.listObjectsV2(bucketName);
		return list.getObjectSummaries();
	}
}
