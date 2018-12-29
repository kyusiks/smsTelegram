package com.anglab.smstelegram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

@SuppressLint("NewApi")
public class NotesDbAdapter {
   private DatabaseHelper mDbHelper;
   private SQLiteDatabase mDb;
   private static final String DATABASE_NAME = "data";

   /**
	TB_SMS01 ���ڸ��
	MODE : sms/mms/bat(���͸�)/cha(����)/mic(��������)/img(mms���̹���)
	DPL : �ߺ�Ȯ��Ű. SMS,MMS�� ��� _id, img�ǰ�� _id-part_id
	SND_NUM : ������� ��ȣ
	SND_NM : ������� �̸�
	RCV_NUM : ������� ��ȣ(���ڵ�����ȣ)
	RCV_DH : �����Ͻ�
	MSG_TXT
	TRN_CHK
	FST_INS_DH
	LST_UPD_DH

	TB_SMS02 ���۸��
	MODE
	DPL
	CON_ID
	LOC_OUT_DH	LOC_OUT_DH ���۽õ��Ͻ�
	TL_COMP_DH	TL_COMP_DH ���ۿϷ��Ͻ�
	MSG_ID

	TB_SMS03 ���� ����
	FIL_ID
	FIL_NM
	USE_YN
	
	TB_SMS04 ���� ����
	FIL_ID
	FIL_SEQ
	FIL_WHO number, text
	FIL_HOW instr, not in, equal
	���� ���� ��� ������ ��� ��

	TB_SMS05 �߼� ��û SMS
	MODE : sms/mms
	DPL : �ߺ�Ȯ��Ű. ��ɾ��� vMsgId
	CON_ID :
	SND_NUM : ������� ��ȣ
	RCV_NUM : ������� ��ȣ(���ڵ�����ȣ) - ���ɹٲ������ ����Ͽ� ����.
	RCV_DH : �����Ͻ�
	MSG_TXT :
	LOC_OUT_DH : ���۽õ��Ͻ�
	SND_COMP_DH : ���ۿϷ��Ͻ�
	LST_UPD_DH



	TB_TL001 ��������
	SET_ID      LID     RID     NICK_NM CON_ID
    SET_NM      ���þ��̵�   Ǫ��             ��Ī             Ŀ�ؼ�
    SEL_MODE    SETTING SETTING SETTING SETTING
    SET_VALUE   1       AdfIE.. GPRO2   1
    SET_CONT
    SORT
    USE_YN
    LST_UPD_DH
	
	TB_TL002 �޴»������
	CID �ڷ��׷�ID
	CID_SEQ �̸�,������ �ٲ𶧸��� �þ�� ���� �ε��� 1,2,3....
	FST_NM
	LST_NM
	TYPE
	LST_UPD_DH
	USE_YN
	
	01 ��������
	02 �ڷ��׷� ID ���� CID
	03 ��ID ���� (���� ������Ʈ �¸�)
	04 CON_ID ����  (���� ������Ʈ �¸�)
	05 ��������....
	
	TB_TL003 ��ID ���� (���� ������Ʈ �¸�)
	BID
	BOT_NM
	USE_YN
	LST_UPD_DH
	
	TB_TL004 CON_ID ����  (���� ������Ʈ �¸�)
	CID
	BID
	CON_ID
	LANG
	USE_YN
	FST_INS_DH
	LST_UPD_DH
	
	
   */ 

   private static final String gv_create_TB_SMS01 /* �޽������_ */ = "CREATE TABLE TB_SMS01 (MODE TEXT, DPL TEXT, SND_NUM TEXT, SND_NM TEXT, RCV_NUM TEXT, RCV_DH TEXT, MSG_TXT TEXT, TRN_CHK TEXT, FST_INS_DH TEXT, LST_UPD_DH TEXT, PRIMARY KEY(MODE, DPL));";
   private static final String gv_create_TB_SMS02 /* ���۸��__ */ = "CREATE TABLE TB_SMS02 (MODE TEXT, DPL TEXT, CON_ID TEXT, LOC_OUT_DH TEXT, TL_COMP_DH TEXT, PRIMARY KEY(MODE, DPL, CON_ID));";
   private static final String gv_create_TB_SMS03 /* ���Ϳ���__ */ = "CREATE TABLE TB_SMS03 (FIL_ID TEXT PRIMARY KEY, FIL_NM TEXT, USE_YN TEXT);";
   private static final String gv_create_TB_SMS04 /* ���ͻ����� */ = "CREATE TABLE TB_SMS04 (FIL_ID TEXT, FIL_SEQ TEXT, FIL_WHO TEXT, FIL_HOW TEXT, PRIMARY KEY(FIL_ID, FIL_SEQ));";
   private static final String gv_create_TB_SMS05 /* �߼ۿ�û��� */ = "CREATE TABLE TB_SMS05 (MODE TEXT, DPL TEXT, CON_ID TEXT, SND_NUM TEXT, RCV_NUM TEXT, RCV_DH TEXT, MSG_TXT TEXT, LOC_OUT_DH TEXT, SND_COMP_DH TEXT, LST_UPD_DH TEXT, PRIMARY KEY(MODE, DPL, CON_ID));";
   
   private static final String gv_create_TB_TL001 /* ��������___*/ = "CREATE TABLE TB_TL001 (SET_ID TEXT PRIMARY KEY, SET_NM TEXT NOT NULL, SEL_MODE TEXT NOT NULL, SET_VALUE TEXT, SET_CONT TEXT, SORT TEXT, USE_YN TEXT, LST_UPD_DH TEXT);";
   private static final String gv_create_TB_TL002 /* �޴»������ */ = "CREATE TABLE TB_TL002 (CID TEXT, CID_SEQ TEXT, FST_NM TEXT, LST_NM TEXT, TYPE TEXT, USE_YN TEXT, LST_UPD_DH TEXT, PRIMARY KEY(CID, CID_SEQ));";
   private static final String gv_create_TB_TL003 /* ������___ */ = "CREATE TABLE TB_TL003 (BID PRIMARY KEY, BOT_NM, USE_YN, LST_UPD_DH);";
   private static final String gv_create_TB_TL004 /* Ŀ�ؼ�����_ */ = "CREATE TABLE TB_TL004 (CID TEXT, BID TEXT, CON_ID TEXT, LANG TEXT, USE_YN TEXT, FST_INS_DH TEXT, LST_UPD_DH TEXT, PRIMARY KEY(CID, BID));";

   
   private static final int DATABASE_VERSION = 3;
   private final Context mCtx;

   @SuppressLint("Override")
   private static class DatabaseHelper extends SQLiteOpenHelper {
       DatabaseHelper(Context context) {
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL("DROP TABLE IF EXISTS TB_SMS01");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS02");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS03");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS04");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS05");
           db.execSQL("DROP TABLE IF EXISTS TB_TL001");
           db.execSQL("DROP TABLE IF EXISTS TB_TL002");
           db.execSQL("DROP TABLE IF EXISTS TB_TL003");
           db.execSQL("DROP TABLE IF EXISTS TB_TL004");
           db.execSQL(gv_create_TB_SMS01);
           db.execSQL(gv_create_TB_SMS02);
           db.execSQL(gv_create_TB_SMS03);
           db.execSQL(gv_create_TB_SMS04);
           db.execSQL(gv_create_TB_SMS05);
           db.execSQL(gv_create_TB_TL001);
           db.execSQL(gv_create_TB_TL002);
           db.execSQL(gv_create_TB_TL003);
           db.execSQL(gv_create_TB_TL004);
       }

       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.d("NotesDbAdapter", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS01");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS02");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS03");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS04");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS05");
           db.execSQL("DROP TABLE IF EXISTS TB_TL001");
           db.execSQL("DROP TABLE IF EXISTS TB_TL002");
           db.execSQL("DROP TABLE IF EXISTS TB_TL003");
           db.execSQL("DROP TABLE IF EXISTS TB_TL004");
           db.execSQL(gv_create_TB_SMS01);
           db.execSQL(gv_create_TB_SMS02);
           db.execSQL(gv_create_TB_SMS03);
           db.execSQL(gv_create_TB_SMS04);
           db.execSQL(gv_create_TB_SMS05);
           db.execSQL(gv_create_TB_TL001);
           db.execSQL(gv_create_TB_TL002);
           db.execSQL(gv_create_TB_TL003);
           db.execSQL(gv_create_TB_TL004);
           
       }
       
	public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.d("NotesDbAdapter", "Downgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS01");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS02");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS03");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS04");
           db.execSQL("DROP TABLE IF EXISTS TB_SMS05");
           db.execSQL("DROP TABLE IF EXISTS TB_TL001");
           db.execSQL("DROP TABLE IF EXISTS TB_TL002");
           db.execSQL("DROP TABLE IF EXISTS TB_TL003");
           db.execSQL("DROP TABLE IF EXISTS TB_TL004");
           db.execSQL(gv_create_TB_SMS01);
           db.execSQL(gv_create_TB_SMS02);
           db.execSQL(gv_create_TB_SMS03);
           db.execSQL(gv_create_TB_SMS04);
           db.execSQL(gv_create_TB_SMS05);
           db.execSQL(gv_create_TB_TL001);
           db.execSQL(gv_create_TB_TL002);
           db.execSQL(gv_create_TB_TL003);
           db.execSQL(gv_create_TB_TL004);
       }
   }

   public NotesDbAdapter(Context ctx) { this.mCtx = ctx; }

   public NotesDbAdapter open() throws SQLException {
       mDbHelper = new DatabaseHelper(mCtx);
       mDb = mDbHelper.getWritableDatabase();
       return this;
   }

   public void close() { mDbHelper.close(); }
   
   public void dbClear() {
	   mDb.execSQL("DROP TABLE IF EXISTS TB_SMS01"); mDb.execSQL(gv_create_TB_SMS01);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_SMS02"); mDb.execSQL(gv_create_TB_SMS02);
	   
   }

   public List<HashMap<String, String>> inqSql(String pMode, HashMap<String, String> pData) {

	   String vQuery = "";
	   String[] vParams = null;
	   if ( pMode == null || "".equals(pMode) ) {
	   } else if ( "SEL01".equals(pMode) ) { // �ߺ� ī��Ʈ. �ƽ� MSG_ID
		   /*vQuery = "SELECT COUNT(1) AS CNT, IFNULL((SELECT MAX(MSG_ID+0) + 1 FROM TB_SMS01), 1) AS SEQ FROM TB_SMS01 "
		         + " WHERE MODE = ? AND DPL = ? AND RCV_DH IS NULL ";
		   vParams = new String[]{ pData.get("MODE"), pData.get("DPL") };*/
		   // RCV_DH �� �յڷ� 1�о� üũ�Ѵ�. ���������⿡ �˳���.
	   } else if ( "SEL02".equals(pMode) ) {
		   vQuery = "SELECT MODE, DPL, MSG_TXT FROM TB_SMS01 WHERE TRN_CHK = 'N' ";

	   } else if ( "SEL03".equals(pMode) ) {
		/*   vQuery = "SELECT A.MSG_ID, A.CID, C.FST_NM, C.LST_NM, C.TYPE, C.CID_SEQ  "
				   	  + "     , B.SND_NUM, B.SND_NM, B.RCV_NUM, B.RCV_DH, B.MSG_TXT "
			   		  + "  FROM TB_SMS02 A, TB_SMS01 B, TB_TL002 C "
			   		  + " WHERE A.MSG_ID = B.MSG_ID AND A.LOC_OUT_DH IS NULL AND A.CID = C.CID(+) AND C.USE_YN = 'Y' "; // �⺻���� �ε�
*/
		   vQuery = "SELECT '' FST_NM, '' LST_NM, '' TYPE, '1' CID_SEQ, A.LOC_OUT_DH, A.TL_COMP_DH, A.CON_ID  "
			   	  + "     , B.MODE, B.SND_NUM, B.SND_NM, B.RCV_NUM, B.RCV_DH, B.DPL "
			   	  + "     , REPLACE(REPLACE(REPLACE(B.MSG_TXT, '&', '&amp;'), '<', '&lt;'), '>', '&gt;') AS MSG_TXT"
			   	  + "     , CASE WHEN B.MODE = 'mms' THEN (SELECT REPLACE(GROUP_CONCAT(REPLACE(DPL, A.DPL || '-', '|')), ',', '')  FROM TB_SMS01 WHERE MODE = 'img' AND DPL LIKE A.DPL || '-%' )   "
			   	  + "            ELSE '' END AS IMG "
		   		  + "  FROM TB_SMS02 A, TB_SMS01 B "
	   		      + " WHERE A.MODE = B.MODE AND A.DPL = B.DPL  "
	   		      + "   AND A.MODE != 'img' "
	   		      + "   AND B.RCV_DH+1 > ((strftime('%s','now','localtime') - 15 * (60 * 60 * 24)) || '000')+1  " // �ֱ�2��ġ�� ������. 
	   		      + "   AND A.TL_COMP_DH IS NULL  "; 
	          //+ "   AND B.SND_NUM = '01199100496'  ";
	          //+ "   AND B.DPL IN( '955','1016')  ";
		   
		   if ( pData != null ) { // Ư�� �޽��� ���̵� ���ؼ� ��ȸ�Ѵٸ�
			   //vQuery += " AND A.MODE = ? AND A.DPL = ? AND A.CID = ? ";
			   //vParams = new String[]{ pData.get("MODE"), pData.get("DPL"), pData.get("CID") };
		   }
		   vQuery += " ORDER BY B.RCV_DH ASC LIMIT 0, 30"; //TODO �⺻���� �ε�

	   } else if ( "SEL04".equals(pMode) ) {
		   vQuery = "SELECT CID, FST_NM, LST_NM, TYPE, CID_SEQ "
				 + "   FROM TB_TL002 WHERE CID = ? AND USE_YN = 'Y'";
		   vParams = new String[]{ pData.get("CID") };

	   } else if ( "SEL05".equals(pMode) ) {
		   vQuery = "SELECT MAX(RCV_DH) AS RCV_DH FROM TB_SMS01 WHERE TRN_CHK = 'Y' ";

	   } else if ( "SEL06".equals(pMode) ) {
		   vQuery = "SELECT COUNT(1) AS CNT FROM TB_SMS05 WHERE MODE = ? AND DPL = ? AND CON_ID = ? ";
		   vParams = new String[]{ pData.get("mod"), pData.get("msgId"), pData.get("conId") };
			
	   } else if ( "SEL_MMSIMG".equals(pMode) ) {
		   vQuery = "SELECT '' FST_NM, '' LST_NM, '' TYPE, '1' CID_SEQ, A.LOC_OUT_DH, A.TL_COMP_DH, A.CON_ID   "
			   	  + "     , B.MODE, B.SND_NUM, B.SND_NM, B.RCV_NUM, B.RCV_DH, B.DPL "
			   	  + "     , REPLACE(REPLACE(REPLACE(B.MSG_TXT, '&', '&amp;'), '<', '&lt;'), '>', '&gt;') AS MSG_TXT"
			   	  + "     , '" + pData.get("msgId") + "' AS MSG_ID, '" + pData.get("orgMsgId") + "' AS ORG_MSG_ID "
		   		  + "  FROM TB_SMS02 A, TB_SMS01 B "
	   		      + " WHERE A.MODE = B.MODE AND A.DPL = B.DPL  "
	   		      + "   AND A.MODE = 'img' AND A.DPL LIKE '" + pData.get("dpl") + "' ";

	   } else if ( "SEL_SNDSMS".equals(pMode) ) { // TODO
		   vQuery = "SELECT B.MODE, B.SND_NUM, B.SND_NM "
		   		  + "  FROM TB_SMS02 A, TB_SMS01 B "
	   		      + " WHERE A.MODE = B.MODE AND A.DPL = B.DPL"
	   		      //+ " AND A.MSG_ID = '" + pData.get("msgId") + "' "
	   		      + "   AND A.MODE = '" + pData.get("mod") + "' AND A.DPL = '" + pData.get("dpl") + "' ";

   
	   } else if ( "SELtext".equals(pMode) ) {

		   vQuery = "SELECT '' FST_NM, '' LST_NM, '' TYPE, '1' CID_SEQ, A.LOC_OUT_DH, A.TL_COMP_DH, A.CON_ID  "
			   	  + "     , B.MODE, B.SND_NUM, B.SND_NM, B.RCV_NUM, B.RCV_DH, B.DPL "
			   	  + "     , REPLACE(REPLACE(REPLACE(B.MSG_TXT, '&', '&amp;'), '<', '&lt;'), '>', '&gt;') AS MSG_TXT"
			   	  + "     , CASE WHEN B.MODE = 'mms' THEN (SELECT REPLACE(GROUP_CONCAT(REPLACE(DPL, A.DPL || '-', '|')), ',', '')  FROM TB_SMS01 WHERE MODE = 'img' AND DPL LIKE A.DPL || '-%' )   "
			   	  + "            ELSE '' END AS IMG "
		   		  + "  FROM TB_SMS02 A, TB_SMS01 B "
	   		      + " WHERE A.MODE = B.MODE AND A.DPL = B.DPL  "
	   		      + "   AND A.MODE != 'img' "
	   		      + "   AND A.MODE = 'mms' AND (SELECT REPLACE(GROUP_CONCAT(REPLACE(DPL, A.DPL || '-', '|')), ',', '')  FROM TB_SMS01 WHERE MODE = 'img' AND DPL LIKE A.DPL || '-%' ) != '' ";
	   		      //+ "   AND B.RCV_DH+1 > ((strftime('%s','now','localtime') - 15 * (60 * 60 * 24)) || '000')+1  ";  // �ֱ�2��ġ�� ������. 
	   		  //    + "   AND A.TL_COMP_DH IS NULL  "; 
	          //+ "   AND B.SND_NUM = '01199100496'  ";
	          //+ "   AND B.DPL IN( '973')  ";
		   
		   if ( pData != null ) { // Ư�� �޽��� ���̵� ���ؼ� ��ȸ�Ѵٸ�
			   //vQuery += " AND A.MODE = ? AND A.DPL = ? AND A.CID = ? ";
			   //vParams = new String[]{ pData.get("MODE"), pData.get("DPL"), pData.get("CID") };
		   }
		   vQuery += " ORDER BY B.RCV_DH ASC LIMIT 0, 1"; //TODO �⺻���� �ε�


   	   } else {
   		   return null;
	   }

	   Cursor result = mDb.rawQuery(vQuery, vParams);
	   List<HashMap<String, String>> vList = aUtil.cur2list(result);
       result.close();
	   return vList;
   }


   public void updList(String pMode, HashMap<String, String> pData) {
	   List<HashMap<String, String>> vList = new ArrayList<>();
	   vList.add(new HashMap<String, String>(pData));
	   updList(pMode, vList);
   }

   // ��������Ʈ ���� �� ������Ʈ
   public void updList(String pMode, List<HashMap<String, String>> pList) {
	   if ( pList == null || pList.isEmpty() ) return;
	   Log.d("updList", "mode : " + pMode + " / " + pList.size() + " row update");

	   String vSql = "";
	   if ( "INS01".equals(pMode) ) { // ���� ������
		   vSql = "INSERT OR REPLACE INTO TB_SMS01 (MODE, DPL, SND_NUM, SND_NM, RCV_NUM, RCV_DH, MSG_TXT, TRN_CHK, FST_INS_DH, LST_UPD_DH) VALUES (?,?,?,?,?,?,?,'N',STRFTIME('%Y%m%d%H%M%S','now','localtime'),STRFTIME('%Y%m%d%H%M%S','now','localtime'))";
	   } else if ( "INS02".equals(pMode) ) { // ���ڸ� ���� �߼� ��� ����
		   vSql = "INSERT OR REPLACE INTO TB_SMS02 (MODE, DPL, CON_ID) VALUES (?, ?, ?)";
	   } else if ( "INS03".equals(pMode) ) { // ���� ����
		   vSql = "INSERT OR REPLACE INTO TB_TL002 (CID, CID_SEQ, FST_NM, LST_NM, TYPE, LST_UPD_DH, USE_YN) VALUES (?, ?, ?, ?, ?, STRFTIME('%Y%m%d%H%M%S','now','localtime'), 'Y')";

	   } else if ( "INS04".equals(pMode) ) { // SMS �߼� ��û
		   vSql = "INSERT OR REPLACE INTO TB_SMS05 (MODE, DPL, CON_ID, SND_NUM, RCV_NUM, RCV_DH, MSG_TXT, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, STRFTIME('%Y%m%d%H%M%S','now','localtime'))";

	   } else if ( "UPD01".equals(pMode) ) { // ���ڸ� ���� �߼� ��� ������ ���� ��
		   vSql = "UPDATE TB_SMS01 SET TRN_CHK = 'Y', LST_UPD_DH = STRFTIME('%Y%m%d%H%M%S','now','localtime') WHERE MODE = ? AND DPL = ?";
	   } else if ( "UPD02".equals(pMode) ) { // ���� ���� �Ϸ�
		   vSql = "UPDATE TB_SMS02 SET TL_COMP_DH = ? WHERE MODE = ? AND DPL = ? AND CON_ID = ? ";
		   //vSql = "UPDATE TB_SMS02 SET CID_SEQ = ?, TL_COMP_DH = ? WHERE MODE = ? AND DPL = ? AND CID = ? ";
	   } else if ( "UPD03".equals(pMode) ) { // �������� �μ�Ʈ �� �����͵��� ��ȿȭ �ݵ�� UPD03->INS03 �Ѽ�Ʈ�� ȣ��ȴ�.
		   vSql = "UPDATE TB_TL002 SET USE_YN = 'N' WHERE CID = ?";
	   } else if ( "UPD04".equals(pMode) ) { // ���ڹ߼۽õ�
		   vSql = "UPDATE TB_SMS02 SET LOC_OUT_DH = ? WHERE MODE = ? AND DPL = ? AND CON_ID = ? ";
	   }

	   mDb.beginTransaction();
	   SQLiteStatement insert = mDb.compileStatement(vSql);

	   if ( "INS01".equals(pMode) ) { // ���� 1row.
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "MODE"   ));
			   insert.bindString(j++, fn_getList(pList.get(i), "DPL"    ));
			   insert.bindString(j++, fn_getList(pList.get(i), "SND_NUM"));
			   insert.bindString(j++, fn_getList(pList.get(i), "SND_NM" ));
			   insert.bindString(j++, fn_getList(pList.get(i), "RCV_NUM"));
			   insert.bindString(j++, fn_getList(pList.get(i), "RCV_DH" ));
			   insert.bindString(j++, fn_getList(pList.get(i), "MSG_TXT"));
			   insert.execute();
		   }
	   } else if ( "INS02".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "MODE"  ));
			   insert.bindString(j++, fn_getList(pList.get(i), "DPL"   ));
			   insert.bindString(j++, fn_getList(pList.get(i), "CON_ID"));
			   insert.execute();
		   }
	   } else if ( "INS03".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "CID"    ));
			   insert.bindString(j++, fn_getList(pList.get(i), "CID_SEQ"));
			   insert.bindString(j++, fn_getList(pList.get(i), "FST_NM" ));
			   insert.bindString(j++, fn_getList(pList.get(i), "LST_NM" ));
			   insert.bindString(j++, fn_getList(pList.get(i), "TYPE"   ));
			   insert.execute();
		   }
	   } else if ( "INS04".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "mod"   ));
			   insert.bindString(j++, fn_getList(pList.get(i), "msgId" )); // �� ���̺��� dpl�� msgId�̴�.
			   insert.bindString(j++, fn_getList(pList.get(i), "conId" ));
			   insert.bindString(j++, fn_getList(pList.get(i), "num"   ));
			   insert.bindString(j++, fn_getList(pList.get(i), "rcvNum"));
			   insert.bindString(j++, fn_getList(pList.get(i), "rcvDh" ));
			   insert.bindString(j++, fn_getList(pList.get(i), "msg"   ));

			   insert.execute();
		   }

	   } else if ( "UPD01".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "MODE"));
			   insert.bindString(j++, fn_getList(pList.get(i), "DPL" ));
			   insert.execute();
		   }
	   } else if ( "UPD02".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "TL_COMP_DH"));
			   insert.bindString(j++, fn_getList(pList.get(i), "MODE"   ));
			   insert.bindString(j++, fn_getList(pList.get(i), "DPL"    ));
			   insert.bindString(j++, fn_getList(pList.get(i), "CON_ID" ));
			   insert.execute();
		   }
	   } else if ( "UPD03".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "CID"));
			   insert.execute();
		   }
	   } else if ( "UPD04".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   int j = 1;
			   insert.bindString(j++, fn_getList(pList.get(i), "LOC_OUT_DH"));
			   insert.bindString(j++, fn_getList(pList.get(i), "MODE"  ));
			   insert.bindString(j++, fn_getList(pList.get(i), "DPL"   ));
			   insert.bindString(j++, fn_getList(pList.get(i), "CON_ID"));
			   insert.execute();
		   }
	   }
	   insert.close();
	   mDb.setTransactionSuccessful();
	   mDb.endTransaction();
   }

   public String fn_getList(HashMap<String, String> map, String pTagname) {
	   if ( map.containsKey(pTagname) ) {
		   return aUtil.nvl(map.get(pTagname));
	   } else {
		   return "";
	   }
   }

   public void updSettingValue(String pSetId, String pSetValue) {
       ContentValues initialValues = new ContentValues();
       initialValues.put("SET_VALUE", pSetValue);
       int i = mDb.update("TB_LC004", initialValues, "SET_ID = '" + pSetId + "'", null);
       if ( i == 0 ) {
           initialValues.put("SET_ID", pSetId);
           initialValues.put("SET_NM", "");
           initialValues.put("SEL_MODE", "");
           initialValues.put("SET_CONT", "");
           initialValues.put("SORT", "");
           initialValues.put("USE_YN", "");
           initialValues.put("LST_UPD_DH", "");
           initialValues.put("SET_NM", "");

    	   mDb.insert("TB_LC004", null, initialValues);
       }
   }
   /********/

   /**** ��������� ���� ****/
   public void insLC000(String pIdSeq, String pSort, String pLstViewNo) {
       ContentValues initialValues = new ContentValues();
       initialValues.put("ID_SEQ", pIdSeq);
       initialValues.put("LST_VIEW_NO", pLstViewNo); // ó�� ������ ������ -1�� ����. ������������ ���߿� ����
       initialValues.put("SORT", pSort);
       mDb.insert("TB_LC000", null, initialValues);
   }

   public boolean updLstViewNo(String pIdSeq, String pLstViewNo) {
       ContentValues args = new ContentValues();
       args.put("LST_VIEW_NO", pLstViewNo);
       return mDb.update("TB_LC000", args, "ID_SEQ='" + pIdSeq + "'", null) > 0;
   }

   public boolean delLC000(String pIdSeq) {
       return mDb.delete("TB_LC000", "ID_SEQ='" + pIdSeq + "'", null) > 0;
   }

   public boolean delLC000All() {
       return mDb.delete("TB_LC000", "1=1", null) > 0;
   }
   /********/
}