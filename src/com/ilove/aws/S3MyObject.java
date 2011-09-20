package com.ilove.aws;

/*
 * Copyright 2010-2011 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */


/**
 * This is a general purpose object used for storing data on S3.
 * The mimeType and data variables need only be set if you are
 * planning to initiate a storage call.  */
public class S3MyObject {

	private String bucketName;
	private byte []data;
	private String storagePath;
	private String mimeType;

	public S3MyObject(byte[] data, String bucketName, String storagePath, String mimeType) {
		this.data = data;
		this.bucketName = bucketName.toLowerCase();
		this.storagePath = storagePath;
		this.mimeType = mimeType;
	}
	
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName.toLowerCase();
	}

	public String getBucketName() {
		return bucketName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data=data;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	/**
	 * Convenience method to construct the URL that points to an object stored on S3 based on the bucket name and storage path.
	 * @return the S3 URL for the object
	 */
	public String getAwsUrl () {
		return "http://"+getBucketName()+".s3.amazonaws.com/"+getStoragePath();
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

}
