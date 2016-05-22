import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class Test implements Runnable{

	/**
	 * @param args
	 */
	
	int max=50;      //������߳���
	int i=0;         //�ظ�����
	int temp;
	ServerSocket serverSocket;
	Socket socket[];
	static BlockingQueue<String> msgQueue=new ArrayBlockingQueue<String>(20);
//	volatile int rownum=1;
	// ��ȡ������
			static final int CountColumnNum = 13;
			 // ����Excel�ĵ�
	       static HSSFWorkbook hwb = new HSSFWorkbook();
	       // sheet ��Ӧһ������ҳ
	       static HSSFSheet sheet = hwb.createSheet("TestPoints");
	       static HSSFRow firstrow = sheet.createRow(0); 
	       static HSSFCell[] firstcell = new HSSFCell[CountColumnNum];
	       static File file=new File("TestPoints.xls");
	
	
	public Test(){
		
		try {
			serverSocket=new ServerSocket(5648);    //��5648�˿ڽ�������
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Server��ʼ��ʧ��,��رշ���!");
			return;
		}
		
		socket=new Socket[max];
		
		System.out.println("waiting for connections");
		try {
			while((socket[i]=serverSocket.accept())!=null){
				temp=i;
//				System.out.println("��"+i+"��socket");
				i++;
				i=i%max;
				new Thread(this).start();           //ÿ������һ���ͻ��˵����ӣ��ͻῪ��һ�������߳�
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
       String[] names = new String[CountColumnNum];
       for(int i=0;i<10;i++){
       	names[i]="��"+(i+1)+"��";
       }
       names[10] = "��ֵ";
       names[11] = "X";
       names[12] = "Y";
       for (int j = 0; j < CountColumnNum; j++) {
           firstcell[j] = firstrow.createCell(j);
           firstcell[j].setCellValue(new HSSFRichTextString(names[j]));
       }
       
   	 Thread t=new Thread(new Test.WriteExcel());
	 t.start();
     new Test();
	

	}

	@Override
	public void run() {
		Socket sk=socket[temp];
		System.out.println("accept:"+sk.getInetAddress().getHostAddress());
		InputStream is=null;
		OutputStream os=null;
		BufferedReader br=null;
		PrintWriter pw=null;   
		String[] check=null;
		StringBuilder con=new StringBuilder();
		try {
			is=sk.getInputStream();
			os=sk.getOutputStream();
			br=new BufferedReader(new InputStreamReader(is));
			pw=new PrintWriter(os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				sk.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}
		String str;
		try {
			while((str=br.readLine())!=null){
				//��ֹ���ݰ�ճ��
				check=str.split(":");
				msgQueue.put(str);
				//����������
				pw.println("Server Received!");
				pw.flush();
			}
		} catch (Exception e) {
		
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	static class WriteExcel implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String str = null;
			String[] inputExcel=null;
			int rownum = 1;
			while (true) {
				try {
					Thread.sleep(500);
					str = msgQueue.take();
					inputExcel = str.split(":");
					System.out.println("��" + rownum + "������");
					System.out.println(str);

					// ������0��ʼ
					HSSFRow row = sheet.createRow(rownum);
					// �õ�Ҫ�����ÿһ����¼
					for (int colu = 0; colu <= 12; colu++) {
						// ��һ����ѭ��
						HSSFCell xh = row.createCell(colu);
						xh.setCellValue(inputExcel[colu]);
					}
					// }
					// �����ļ��������׼��������ӱ��
					OutputStream out = new FileOutputStream(file);
					hwb.write(out);
					System.out.println("��" + rownum + "�����ݵ���excel�ɹ�");
					rownum++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	  
	}


}

