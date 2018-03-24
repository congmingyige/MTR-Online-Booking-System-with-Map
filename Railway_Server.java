
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Railway_Server extends JFrame {
	ServerSocket serverSocket=null;
	Socket client=null;
	JTextArea text;
	JScrollPane jsp;
	JButton jb_info=new JButton("站点查询情况");
	JPanel jp_north=new JPanel();
	JPanel jp_center=new JPanel();
	private class Node {
		Node next=null;
		int d;
	}
	Node route[]=new Node[100];
	public Railway_Server() throws IOException {
		
		//MySQL
		try {
			//create database
			Railway_MySQL.regDriver();
			Railway_MySQL.conBuild();
			
			if (Railway_MySQL.ifexistDatabase("railway")==false) {
				System.out.println("create database 'railway'");
				Railway_MySQL.execUpdate("create DATABASE railway");
				//Railway_MySQL.execUpdate("create DATABASE if not exists Railway");
			}
			Railway_MySQL.closeDB();
			
			//create table
			Railway_MySQL.changeUrl();
			Railway_MySQL.regDriver();
			Railway_MySQL.conBuild();
			
			if (Railway_MySQL.ifexistTable("station")==false) {
				System.out.println("create table 'station'");
				Railway_MySQL.createTableRailway();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		int s,t;
		Node node;
		Scanner sc;
		sc=new Scanner(ClassLoader.getSystemClassLoader().getResourceAsStream("route.txt"));
		//sc=new Scanner("/route.txt");
		for (int i=0;i<100;i++) {
			route[i]=null;
		}
		while (sc.hasNext()) {
			s=sc.nextInt();
			t=sc.nextInt();
			node=new Node();
			node.next=route[s];
			node.d=t;
			route[s]=node;
			node=new Node();
			node.next=route[t];
			node.d=s;
			route[t]=node;
		}
		sc.close();
		
		text=new JTextArea(10,20);
		text.setLineWrap(true);
		jsp=new JScrollPane();
		jsp.setViewportView(text);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jp_north.add(jb_info);
		jp_center.add(jsp);
		setLayout(new BorderLayout());
		add(jp_north,BorderLayout.NORTH);
		add(jp_center,BorderLayout.CENTER);
		
		setBounds(550,100,360,360);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		jb_info.addActionListener(new ActionListener() {
			@Override //check
			public void actionPerformed(ActionEvent e) {
//				ResultSet rs=Railway_MySQL.execQuery("select * from railway");
//				StringBuffer info=new StringBuffer();
//				try {
//					while (rs.next()) {
//						info.append(rs.getInt(1)).append(' ').append(rs.getInt(2));
//						JOptionPane.showMessageDialog(null,info,"站点访问",JOptionPane.PLAIN_MESSAGE);
//					}
//				} catch (SQLException e1) {
//					e1.printStackTrace();
//				}
			}
		});
		
		
		text.append(new Date().toString() + "\n");
		text.append("等待连接...\n");
		text.setCaretPosition(text.getText().length());
		
		//ServerSocket serverSocket=new ServerSocket(0);	//系统提供一个默认空闲的端口
		//int port=serverSocket.getLocalPort();
//			String line=new String("");			
//			BufferedReader br=new BufferedReader(new InputStreamReader(
//				Runtime.getRuntime().exec("netstat -aon | findstr 4444").getInputStream()));
//			while ((line=br.readLine())!=null) {
//				System.out.println(line);
//				//get process id
//				Runtime.getRuntime().exec("taskkill /F /pid xxxx");
//			}
//			//但是无法识别netstat -aon | findstr 4444，但能识别ipconfig等		//固定端口号要告知客户端，所以端口号一般是不变的
		try {
			serverSocket=new ServerSocket(4444);
		} catch(Exception e) {
			//如果有进程占用该端口，则ServerSocket无法创建，执行IOException块
			//Runtime.getRuntime().exec("taskkill /F /pid xxxx");
			try {
				Runtime.getRuntime().exec("cmd /c start Port.bat");
				Thread.sleep(1000); //足够长的时间让bat文件执行完毕
				serverSocket=new ServerSocket(4444);
			} catch(Exception ee) {
				e.printStackTrace();
			}
		}
		//System.out.println(serverSocket.getLocalPort());
		
		while (true) {
			
			try {
				client=serverSocket.accept();
				System.out.println("new user");
				System.out.println("客户地址：" + client.getInetAddress());
				text.append("客户地址：" + client.getInetAddress() + "连接\n");
				text.setCaretPosition(text.getText().length());
			} catch(IOException e) {
				System.out.println("正在等待用户");
			}
			if (client!=null) {
				new ServerThread(client).start();
			} else {
				continue;
			}
			
			System.out.println("xxxxx");
		}		
	}
	class ServerThread extends Thread {
		Socket socket;
		DataOutputStream out=null;
		DataInputStream in=null;
		int source,destination;
		int pre[]=new int[1000];
		ServerThread(Socket t) {
			socket=t;
			try {
				in=new DataInputStream(socket.getInputStream());
				out=new DataOutputStream(socket.getOutputStream());
			} catch(IOException e) {
				
			}
		}
		public void run() {
			while (true) {
				try {
					source=in.readInt();
					destination=in.readInt();
//					ResultSet rs=Railway_MySQL.execQuery("select * from railway where station='"+source+"'");
//					try {
//						//source
//						if (!rs.next())
//							Railway_MySQL.execUpdate("insert into station values("+source+")");
//						Railway_MySQL.execUpdate("update station set visit=visit+1" + "where station_id=" + source);
//						//destination
//						if (!rs.next())
//							Railway_MySQL.execUpdate("insert into station values("+destination+")");
//						Railway_MySQL.execUpdate("update station set visit=visit+1" + "where station_id=" + destination);						
//					} catch(SQLException e) {
//						
//					}

					System.out.println("source="+source+" destination="+destination);
					findPath();
					text.append(client.getInetAddress() + " send data: " + source + " " + destination + " " + "\n");
					text.setCaretPosition(text.getText().length());
					
					//数据库记录站点搜索次数，判断站点人流量。如果搜索站点次数激增，要人工排查是否出现特殊情况。
					
					//宽度优先搜索,但一般数据比较少，查询次数比较多的，会进行初始化计算
					System.out.println("source="+source+" destination="+destination);
					findPath();
					int d=source;
					while (d!=destination) {
						out.writeInt(d);
						d=pre[d];
					}
					out.writeInt(destination);
					out.writeInt(-1); //-1作为结束标志					
				} catch(IOException e) {
					System.out.println("客户离开");
					break;
				}
			}
//			try {
//				in.close();
//				out.close();
//			} catch(IOException e) {
//				System.out.println("文件无法关闭");
//			}
		}
		public void findPath() {
			//所有的路径长度都为1
			int head,tail,queue[]=new int[1000];
			Node node;
			pre[destination]=destination;
			queue[1]=destination;
			head=0;
			tail=1;
			for (int i=0;i<100;i++)
				pre[i]=-1;
			while (head<=tail) {
				head++;
				node=route[queue[head]];
				while (node!=null) {
					if (pre[node.d]==-1) {
						//System.out.println(node.d);
						tail++;
						queue[tail]=node.d;
						pre[node.d]=queue[head];
						if (node.d==source)
							return ;				
					}
					node=node.next;
				}
			}
		}
	}
	public static void main(String[] args) throws IOException {
		Railway_Server railway_Server=new Railway_Server();
	}
}
