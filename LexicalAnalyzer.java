import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LexicalAnalyzer {
	//private static final Path pathSource = Paths.get("E:/testdata/sorceCode.txt");
	private static String sourcePath;
	private static String targetPath;
	private static String errortPath;
	//static String dirPath = "E:/testdata/Compiler";
	ArrayList<String> errContext = new ArrayList<String>();//记录单次的报错信息
	ArrayList<DualisticFormula> dualisticFormulas = new ArrayList<DualisticFormula>();//所有二元式存放在这个表中
	public static void main(String agrs[]){

		String path;
		System.out.println("Input the path of a pas file:(eg:E:\\testdata\\Compiler\\sorceCode.pas)");
		Scanner scan = new Scanner(System.in);
		if(scan.hasNext()) {
			path = scan.next();
			File tmpFile = new File(path);
			String fileName,filePath;
			String fileHead,fileTail;
			if(!tmpFile.exists()) {
				System.out.println( path+" dosen't exist");
			}
			else {
				
				fileName = tmpFile.getName();
				filePath = tmpFile.getParent();
				String data[] = fileName.split("\\.");
				fileHead = data[0];
				fileTail = data[1];
				fileHead = filePath+"\\"+fileHead;
				if(fileTail.equals("pas")) {
					LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
					lexicalAnalyzer.getPath(fileHead);
					lexicalAnalyzer.analyse();
					System.out.println("Lexical Analist finished");
				}
				else System.out.println(fileName+" is not a pas file");
				
			}
			scan.close();
		}

		
		
	}
	public void getPath(String fileHead) {
		sourcePath = fileHead + ".pas";
		targetPath = fileHead + ".dyd";
		errortPath = fileHead + ".err";
		
	}
	public void analyse() {
		File file = new File(sourcePath);//打开源代码文件
		int lineNum = 1;//行号
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			init();	//创建二元式文件和err文件
			String line = reader.readLine();
			DualisticFormula end = new DualisticFormula();
			end.setLineEnd();
			while (line != null) {
				//分析一行源代码，并将产生的二元式和错误信息保存到两张表中
				analyzeALine(line,lineNum);
				dualisticFormulas.add(end);//添加行结束标志
				line = reader.readLine();
				lineNum ++;
			}
           writeResult(dualisticFormulas, errContext);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void writeResult(ArrayList<DualisticFormula> dAList, ArrayList<String> errList) {
		try {
		FileWriter writTatg = new FileWriter(targetPath);
		FileWriter writErr = new FileWriter(errortPath);
		for(int i = 0;i < dAList.size();i++) {
			if(dAList.get(i).isEnd()) { //如果是行结束
				writTatg.write("            EOLN 24");//表示写完了一行
			}
			else {
				writTatg.write(dAList.get(i).token);
				writTatg.write(" ");
			    writTatg.write(dAList.get(i).type);
			}			
			writTatg.write('\n');					
		}
	    writTatg.write("             EOF 25"); //表示写完了整个源代码文件
		writTatg.close();
		for(int i = 0;i < errList.size();i++) {
			writErr.write(errList.get(i)+'\n');
		}      
		writErr.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void init() {//创建生成的目标码文件并生成error文件
//		File dir = new File(dirPath);
		File targ = new File(targetPath);
		File err = new File(errortPath);
		try {
//		if(!dir.exists()) 
//			dir.mkdirs();
		if(!targ.exists())
			targ.createNewFile();
		if(!err.exists())
			err.createNewFile();		
	}catch (IOException e) {
		e.printStackTrace();
	}
   }
	
	private void analyzeALine(String line,int lineNum) {		
	   char []content = new char[line.length()];//读到的一行源代码存在此处
	   int label = 0;//当前指针指向本行第一个字符
	   for(int i=0;i < line.length();i++) {
		   content[i] = line.charAt(i);
	   }	   
	  while(label < line.length()) {//遍历这一行
		  DualisticFormula tmpDF = new DualisticFormula();
		  //boolean isFoward = false;//判断当前字符是否已经被读取
		  switch (content[label]) {//检查当前指针
		case '=':                  //是等号
			tmpDF.token[0] = content[label++];
			tmpDF.type[0] = '1';
			tmpDF.type[1] = '2';
			dualisticFormulas.add(tmpDF);
			break;
		case '-':                  //是减号
			tmpDF.token[0] = content[label++];
			tmpDF.type[0] = '1';
			tmpDF.type[1] = '8';
			dualisticFormulas.add(tmpDF);
			break;
		case '*':                  
			tmpDF.token[0] = content[label++];
			tmpDF.type[0] = '1';
			tmpDF.type[1] = '9';
			dualisticFormulas.add(tmpDF);
			break;
		case '(':                                // (  21
			tmpDF.token[0] = content[label++];
			tmpDF.type[0] = '2';
			tmpDF.type[1] = '1';
			dualisticFormulas.add(tmpDF);
			break;
		case ')':                                //)   22
			tmpDF.token[0] = content[label++];
			tmpDF.type[0] = '2';
			tmpDF.type[1] = '2';
			dualisticFormulas.add(tmpDF);
			break;
		case ';':                                 //;  23         
			tmpDF.token[0] = content[label++];
			tmpDF.type[0] = '2';
			tmpDF.type[1] = '3';
			dualisticFormulas.add(tmpDF);
			break;
		case ' ':                              //读到空格时，读下一个符号       
            label++;
			break;
		case '<':                  
			tmpDF.token[0] = content[label++];      
			switch (content[label]) {
			case '=':                          //<=         14 
			    tmpDF.token[1] = content[label++];
				tmpDF.type[0] = '1';
				tmpDF.type[1] = '4';
				break;
			case'>':                           //<>          13
				tmpDF.token[1] = content[label++];
				tmpDF.type[0] = '1';
				tmpDF.type[1] = '3';
				break;
			default:                          //<               15
				tmpDF.type[0] = '1';
				tmpDF.type[1] = '5';
				break;
			}
			dualisticFormulas.add(tmpDF);
			break;
		case '>':                             
			tmpDF.token[0] = content[label++];       
			switch (content[label]) {
			case '=':                          //>=   16
			    tmpDF.token[1] = content[label++];
				tmpDF.type[0] = '1';
				tmpDF.type[1] = '6';
				break;
			default:                          //>   17
				tmpDF.type[0] = '1';
				tmpDF.type[1] = '7';
				break;
			}
			dualisticFormulas.add(tmpDF);
			break;			
		case ':':                             
			tmpDF.token[0] = content[label++];  
			tmpDF.token[1] = '=';
			tmpDF.type[0] = '2';
			tmpDF.type[1] = '0';               //:=   20
			dualisticFormulas.add(tmpDF);
			switch (content[label]) {
			case '=':                          
			    label++;
				break;
			default:                          //   error
				String error = "<ERROR>LINE:  "+ lineNum + " the symbol "+"':=' "+"is spelled incorrectly.";
				errContext.add(error);
				break;
			}			
			break;	
		default:
			if(content[label] >='0'&& content[label]<='9')    //是常数   11
			{
				tmpDF.type[0] = '1';
				tmpDF.type[1] = '1';
				int i = 0;    //数字位数
				tmpDF.token[i++] = content[label++];
				while(label < line.length() && content[label] >='0'&& content[label]<='9') {
					if(i > 15) { //常数长度过长
						String error = "<ERROR>LINE:  "+ lineNum + " the length of constnt oversteped the boundary.";
						errContext.add(error);
						break;
					}
					tmpDF.token[i++] = content[label++];
				}
				dualisticFormulas.add(tmpDF);
				break;
			}
			else if(content[label]>='a'&&content[label]<='z')     //是字母
			{
				int i = 0;    //数字位数
				tmpDF.token[i++] = content[label++];
				//当匹配到字母或者数字且没越界时循环
				int basicFlag = 0;//标识是否是基本字
				while(label < line.length() && ((content[label] >='0'&& content[label]<='9')||(content[label] >='a'&& content[label]<='z'))) {
					if(i > 15) { //当越界时
						String tmp = new String(tmpDF.token);
						String error = "<ERROR>LINE:  "+ lineNum + " the length of symbol '"+tmp+"...' oversteped the boundary.";
						errContext.add(error);
						while(label < line.length()&&!isSymbol(content[label])) {
							label++;
						}
						break;
					}
					if(content[label] >='0'&& content[label]<='9') {  //当遇到了数字
					    basicFlag = confirmBasicCharacter(tmpDF.token);
					    if(basicFlag == 0) {//可确定当前串是标识符  10
					    	tmpDF.type[0] = '1';
					    	tmpDF.type[1] = '0';
					    }
					    else { //是基本串
					    	tmpDF.setBasic(basicFlag);
					    	break;
					    }
					}
					tmpDF.token[i++] = content[label++];
				}
				//扫描结束了
					 basicFlag = confirmBasicCharacter(tmpDF.token);
					   if(basicFlag == 0) {//可确定当前串是标识符  10
					    	tmpDF.type[0] ='1';
					    	tmpDF.type[1] ='0';
					    }
					    else //是基本串
					    	tmpDF.setBasic(basicFlag);			
					dualisticFormulas.add(tmpDF);
					break;
				}
			else {
				//将其当做标识符
				int i=0;//计数
				while(!isSymbol(content[label]) && label <line.length() && i <= 15) {
					tmpDF.token[i++] = content[label++];
				}
				tmpDF.type[0]='1';
				tmpDF.type[1]='0';
				dualisticFormulas.add(tmpDF);
				//String message = new String(tmpDF.token);
				String error = "<ERROR>LINE:  "+ lineNum + " '" +tmpDF.token[0]+"...' is an illegal symbol.";
				errContext.add(error);
                if(i > 15) {
                	while(!isSymbol(content[label]) && label < line.length())label++;
                }
				break;
			}
			}	  
		}
	  }
	private boolean isSymbol(char label) {
		if(label=='='||label =='<'||label=='>'||label=='-'||label=='*'||label=='('||label==')'||label==';'||label==':'||label==' ')
			return true;
		else return false;
	}
	int confirmBasicCharacter(char[] letter) {//确认串是否是基本字
		String context = new String(letter);
		if(context.equals("begin           "))return 1;
		else if(context.equals("end             ")) return 2;
		else if(context.equals("integer         ")) return 3;
		else if(context.equals("if              ")) return 4;
		else if(context.equals("then            ")) return 5;
		else if(context.equals("else            ")) return 6;
		else if(context.equals("function        ")) return 7;
		else if(context.equals("read            ")) return 8;
		else if(context.equals("write           ")) return 9;
		else return 0;
	}	
}

class DualisticFormula{
	char[] token;
	char[] type;
	public DualisticFormula(){
		token = new char[16];
		type = new char[2];
        clear();
	}
	public void clear() {
		for(int i=0; i<16; i++) token[i] = ' ';
		for(int i=0; i<2;i++) type[i] = ' ';
	}
	public void setBasic(int basicType) {//根据基本字类型更新type内容
		switch(basicType) {
		case 1:
			type[0]=' ';
			type[1]='1';
			break;
		case 2:
			type[0]=' ';
			type[1]='2';
			break;
		case 3:
			type[0]=' ';
			type[1]='3';
			break;
		case 4:
			type[0]=' ';
			type[1]='4';
			break;
		case 5:
			type[0]=' ';
			type[1]='5';
			break;
		case 6:
			type[0]=' ';
			type[1]='6';
			break;
		case 7:
			type[0]=' ';
			type[1]='7';
			break;
		case 8:
			type[0]=' ';
			type[1]='8';
			break;
		case 9:
			type[0]=' ';
			type[1]='9';
			break;
		default:
				break;
		}		
	}
    public void setLineEnd() {
    	type[0] = '2'; type[1] = '4';
    } 
    public void setFileEnd() {
    	type[0] = '2'; type[1] = '5';
    }
    public boolean isEnd() {//判断是否是行结束
    	if(type[0]=='2'&&type[1]=='4')return true;
    	return false;
    }
}