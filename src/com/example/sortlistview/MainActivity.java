package com.example.sortlistview;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts.Photo;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sortlistview.SideBar.OnTouchingLetterChangedListener;

public class MainActivity extends Activity {
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private ClearEditText mClearEditText;
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID };
	private ArrayList<SortModel> mrContacts = new ArrayList<SortModel>();
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;
	private static final int PHONES_NUMBER_INDEX = 1;
	private static final int PHONES_PHOTO_ID_INDEX = 2;
	private static final int PHONES_CONTACT_ID_INDEX = 3;

	/**
	 * ����ת����ƴ������
	 */
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;

	/**
	 * ����ƴ��������ListView�����������
	 */
	private PinyinComparator pinyinComparator;
//	private String[] dateStrings;
	private SortModel[] dateStrings;
	private RelativeLayout moreMsg;
	private ImageView headerImageView;
	private TextView nameTextView;
	private TextView numberTextView;
	private Button setbButton;
	private Button backButton;
	private String userNameString;
	private String userPhone;
	private Bitmap photoBitmap ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		String numberString = intent.getStringExtra("PhoneNumber");
		//�����ж�
		if(numberString != null){
			// ����
			 Intent callIntent=new Intent();
			 callIntent.setAction("android.intent.action.CALL");
			 callIntent.setData(Uri.parse("tel:"+numberString.toString()));
			 startActivity(callIntent);
		}
		moreMsg = (RelativeLayout) findViewById(R.id.moreMsg);
		headerImageView = (ImageView) findViewById(R.id.img_head);
		nameTextView = (TextView) findViewById(R.id.phoneName);
		numberTextView = (TextView) findViewById(R.id.phoneNumber);
		setbButton = (Button) findViewById(R.id.toSet);
		backButton = (Button) findViewById(R.id.toBack);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (moreMsg.getVisibility() == View.VISIBLE) {
					moreMsg.setVisibility(View.GONE);
				} else {

				}
			}
		});
		setbButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				createShortCut();
			}
		});
		getPhoneContacts();
		dateStrings = new SortModel[mrContacts.size()];
		for (int i = 0; i < mrContacts.size(); i++) {
			dateStrings[i] = mrContacts.get(i);
		}
		// startActivity(new Intent(this,PhoneList.class));
		initViews();
	}

	private void initViews() {
		// ʵ��������תƴ����
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		// �����Ҳഥ������
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// ����ĸ�״γ��ֵ�λ��
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// ����Ҫ����adapter.getItem(position)����ȡ��ǰposition����Ӧ�Ķ���
				// Toast.makeText(getApplication(),
				// ((SortModel) adapter.getItem(position)).getName(),
				// Toast.LENGTH_SHORT).show();
				if (moreMsg.getVisibility() == View.GONE) {
					userNameString = SourceDateList.get(position).getName();
					userPhone = SourceDateList.get(position).getNumber();
					photoBitmap = SourceDateList.get(position).getPhoto();
					headerImageView.setImageBitmap(photoBitmap);
					nameTextView.setText(userNameString);
					numberTextView.setText(userPhone);
					moreMsg.setVisibility(View.VISIBLE);

				} else {
				}
			}
		});

		SourceDateList = filledData(mrContacts);

		// ����a-z��������Դ����
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);

		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

		// �������������ֵ�ĸı�����������
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// ������������ֵΪ�գ�����Ϊԭ�����б�����Ϊ���������б�
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void getPhoneContacts() {
		ContentResolver resolver = this.getContentResolver();

		// ��ȡ�ֻ���ϵ��
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {

				// �õ��ֻ�����
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// ���ֻ�����Ϊ�յĻ���Ϊ���ֶ� ������ǰѭ��
				if (TextUtils.isEmpty(phoneNumber))
					continue;

				// �õ���ϵ������
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);

				// �õ���ϵ��ID
				Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

				// �õ���ϵ��ͷ��ID
				Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);

				// �õ���ϵ��ͷ��Bitamp
				Bitmap contactPhoto = null;

				// photoid ����0 ��ʾ��ϵ����ͷ�� ���û�и���������ͷ�������һ��Ĭ�ϵ�
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				} else {
					// Ĭ��ͷ��
					contactPhoto = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_launcher);
				}

				SortModel mContact = new SortModel();
				mContact.setName(contactName) ;
				mContact.setNumber(phoneNumber) ;
				mContact.setPhoto(contactPhoto)  ;
				mrContacts.add(mContact);
			
			}

			phoneCursor.close();
		}
	}

	/**
	 * ΪListView�������
	 * 
	 * @param date
	 * @return
	 */
	private List<SortModel> filledData(List<SortModel> date) {
		List<SortModel> mSortList = new ArrayList<SortModel>();

		for (int i = 0; i < date.size(); i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(date.get(i).getName());
			sortModel.setNumber(date.get(i).getNumber());
			sortModel.setPhoto(date.get(i).getPhoto());
			
			// ����ת����ƴ��
			String pinyin = characterParser.getSelling(date.get(i).getName());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * ����������е�ֵ���������ݲ�����ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<SortModel> filterDateList = new ArrayList<SortModel>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (SortModel sortModel : SourceDateList) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		// ����a-z��������
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	/** ������ݷ�ʽ **/
	private void createShortCut() {
		// ���жϸÿ���Ƿ����
		if (!isExist()) {
			Intent intent = new Intent();
			// ָ����������
			intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			// ָ����ݷ�ʽ��ͼ��
//			Parcelable icon = Intent.ShortcutIconResource.fromContext(this,
//					R.drawable.logo);
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, photoBitmap);
			// ָ����ݷ�ʽ������
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, userNameString);
			// ָ�����ͼ�꼤���ĸ�activity
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.putExtra("PhoneNumber", userPhone);
			ComponentName component = new ComponentName(this,
					MainActivity.class);
			i.setComponent(component);
			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);

			sendBroadcast(intent);
		}
	}

	private boolean isExist() {
		boolean isExist = false;
		int version = getSdkVersion();
		Uri uri = null;
		if (version < 2.0) {
			uri = Uri
					.parse("content://com.android.launcher.settings/favorites");
		} else {
			uri = Uri
					.parse("content://com.android.launcher2.settings/favorites");
		}
		String selection = " title = ?";
		String[] selectionArgs = new String[] { userNameString };
		Cursor c = getContentResolver().query(uri, null, selection,
				selectionArgs, null);

		if (c != null && c.getCount() > 0) {
			isExist = true;
		}

		if (c != null) {
			c.close();
		}

		return isExist;
	}

	private int getSdkVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
