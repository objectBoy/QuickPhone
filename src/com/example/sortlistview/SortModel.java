package com.example.sortlistview;

import android.graphics.Bitmap;

public class SortModel {

	private String name;   //��ʾ������
	private String sortLetters;  //��ʾ����ƴ��������ĸ
	private String number;
	private Bitmap photo;
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public Bitmap getPhoto() {
		return photo;
	}
	public void setPhoto(Bitmap photo) {
		this.photo = photo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
