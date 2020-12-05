import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RecursiveDescentParser {
    List<Token> tokens = new ArrayList<Token>();
	List<process> proTab = new ArrayList<process>();
	List<Vari> variTab = new ArrayList<Vari>();
	List<String> errTab = new ArrayList<String>();
	Token currToken;
	int errLine;
	private int pointer;
	private static String dydPath;
	private static String dysPath;
	private static String errPath;
	private static String proPath;
	private static String varPath;
	public static void main(String agrs[]){

		String path;
		System.out.println("Input the path of a pas file:(eg:E:\\testdata\\Compiler\\sorceCode.dyd)");
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
				if(fileTail.equals("dyd")) {
					RecursiveDescentParser parser = new RecursiveDescentParser();
					parser.getPath(fileHead);
			        parser.parse();
					System.out.println("Parsing finished");
				
				}
				else System.out.println(fileName+" is not a dyd file");			
			}
			scan.close();
		}
	}
	public void getPath(String fileHead) {
		dydPath = fileHead + ".dyd";
		dysPath = fileHead + ".dys";
		errPath = fileHead + ".err";
		proPath = fileHead + ".pro";
		varPath = fileHead + ".var";
	}
	public void parse() {
		init();
		prcedure();
		writeResult();
	}
	private void writeResult() {
		try {
			FileWriter errWriter = new FileWriter(errPath, true);
			FileWriter proWriter = new FileWriter(proPath);
			FileWriter varWriter = new FileWriter(varPath);
			FileWriter dysWriter = new FileWriter(dysPath);
			for(int i =0; i < errTab.size();i++) {
				errWriter.write(errTab.get(i));
				if(i != errTab.size() -1) errWriter.write('\n');
			}
			errWriter.close();			
			proWriter.write("ProcessName" + blanks(7));//11
			proWriter.write("ProcessType" + blanks(7));
			proWriter.write("ProcessLevel" + blanks(6));
			proWriter.write("FirstAddress" + blanks(6));			
			proWriter.write("LastAddress\n");           
			for(int i=0; i < proTab.size();i++) {
				process pro = proTab.get(i);
				int firAdr =-1,lastAdr=-1;
				for(int j=0;j < variTab.size();j++) {
					if(variTab.get(j).vProc.equals(pro.pName)) {
						if(firAdr == -1) firAdr = j;
						lastAdr = j;
					}
				}
				proWriter.write(pro.pName);
				proWriter.write(blanks(18 - pro.pName.length()));
				proWriter.write(pro.pType + blanks(18-pro.pType.length()));
				proWriter.write(Integer.toString(pro.pLev) + blanks(18- Integer.toString(pro.pLev).length()));
				proWriter.write(Integer.toString(firAdr) + blanks(18-Integer.toString(firAdr).length()));
				proWriter.write(Integer.toString(lastAdr));
				if(i != proTab.size()-1) proWriter.write('\n');
			}
			proWriter.close();
			varWriter.write("VariateName" + blanks(7));
			varWriter.write("VariateProcess" + blanks(4));
			varWriter.write("VariateKind" + blanks(2));
			varWriter.write("VariateLevel" + blanks(2));
			varWriter.write("VariateAddress\n");
			for(int i=0; i < variTab.size();i++) {
				Vari var = variTab.get(i);
				varWriter.write(var.vName + blanks(18- var.vName.length()));
				varWriter.write(var.vProc + blanks(18 - var.vProc.length()));
				varWriter.write(Integer.toString(var.vKind) + blanks(12));
				varWriter.write(Integer.toString(var.vLev) + blanks(14-Integer.toString(var.vLev).length()));
			    varWriter.write(Integer.toString(i));
			    if(i != variTab.size()-1)varWriter.write("\n");
			}
			varWriter.close();
			for(int i = 0;i < tokens.size();i++) {
				Token token = tokens.get(i);
				if(token.type <=23) {
					dysWriter.write(token.character + blanks(17-token.character.length())+Integer.toString(token.type));					
				}
				else if(token.type==24) {
					dysWriter.write(blanks(12)+token.character+" "+Integer.toString(token.type));
				}
				else if(token.type==25) {
					dysWriter.write(blanks(13)+token.character+" "+Integer.toString(token.type));
				}
				if(i != tokens.size()-1)
				dysWriter.write('\n');
			}
			dysWriter.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String blanks(int blkNum) {
		String blank = new String();
		blank = " ";
		for(int i=0;i < blkNum - 1;i++)
			blank += " ";
		return blank;
	}
	private void prcedure() {
		block();
		//if(currToken.type != 25)error(27);
	}
	private void block() {
		adavance(1);//begin
		if(!fillInPro("main", "main")) error(26);
		stateTable();
		adavance(23);//;
		implementTable();
		adavance(2);//end
	}
	private void stateTable() {
		statement();
		stateTable_();
	}
	private void stateTable_(){
		if(lookAhead(1) == 3 || currToken.type==3) {//   integer
			adavance(23);//;
			statement();
			stateTable_();
		}
	}
	private void statement() {
		if(lookAhead(1) == 7) //function
			funcState();
			else varState();
	}
	private void varState() {
		adavance(3); // integer
		if(currToken.type == 10) {
			if(! fillInVar(currToken.character, 0))error(24);
		}
		variate();
	}
	private void variate() { 
		identifier();
	}
	private void identifier() {
		adavance(10);
		}
	private void funcState() {
		boolean isFilled = false;
		String tmpProName = new String();		
		adavance(3);//integer
		adavance(7);//function
		if(currToken.type == 10) {//it is an identifier			
			isFilled = fillInPro(currToken.character, "integer");
			tmpProName = currToken.character;
			if(!isFilled) error(25);
		}
		identifier();
		adavance(21);//    (
		if(currToken.type == 10) {//it is an identifier
			if(!fillInVar(currToken.character, 1))error(24);	
		}
		parameter();
		adavance(22);//       )
		adavance(23);//;
		func();
		if(isFilled) finishPro(tmpProName);
	}
	private void parameter() { variate();}
	private void func() {
		adavance(1);//begin
		stateTable();
		adavance(23);//;
		implementTable();
		adavance(2);//end
	}
	private void implementTable() { 
		implement(); 
		implementTable_();
	}
	private void implementTable_() {
		//read,write,:=,if
		if(lookAhead(1) == 8|| lookAhead(1)==9||lookAhead(2)==20||lookAhead(1)==4
		|| currToken.type==8|| currToken.type==9|| lookAhead(1)==20||currToken.type==4) {
			adavance(23);//;
			implement();
			implementTable_();
		}
	}
	private void implement() {
		switch (currToken.type) {
		case 8://read
			reState();
			break;
		case 9://write
			wrState();
			break;
		case 4://if
			conState();
			break;
		case 10://identifier
			asState();
			break;
		default:
			error(28);
			break;
		}
	}
	private void reState() {
		adavance(8);//read
		adavance(21);// (
		if(!isVariDefined(currToken.character)&&currToken.type==10&&!isProDefined(currToken.character)) error(27);
		variate();
		adavance(22);//  )
	}
	private void wrState() {
		adavance(9);//write
		adavance(21);//  (
		if(!isVariDefined(currToken.character)&&currToken.type==10&&!isProDefined(currToken.character)) error(27);
		variate();
		adavance(22); //    )
	}
	private void asState() {
		if(!isVariDefined(currToken.character)&&currToken.type==10&&!isProDefined(currToken.character)) error(27);
		variate();		
		adavance(20);//  :=
		arithExpression();
	}
	private void arithExpression() {
		term();  
		arithExpression_();
	}
	private void arithExpression_() {
		if(currToken.type == 18) {// -
			match();  
			term();
			arithExpression_();
		}
	}	
	private void term() {
		factor(); 
		term_();
	}
	private void term_() {
		if(currToken.type == 19) {//*
			match();
			factor();
			term_();
		}
	}
	private void factor() {
		if(currToken.type == 11) constant();// a constant
		else if(lookAhead(1) == 21) callFunc();// (
		else {
			if(!isVariDefined(currToken.character)&&currToken.type==10)error(27);
			variate();
		}
	}
	private void constant() {
		if(currToken.type == 11) match();//a constant
		else error(11);//not a constant			
	}
	private void callFunc() {
		if(!isProDefined(currToken.character)&&currToken.type==10)error(27);
		identifier();
		adavance(21);  //   (
		arithExpression();
		adavance(22);   //  )
	}
	private void conState() {
		adavance(4);//if
		// not constant,not a variable, not process
		if(currToken.type==10 && !isProDefined(currToken.character) &&!isVariDefined(currToken.character))
			error(27);
		conExpression();
		adavance(5);//then
		implement();
		adavance(6);//else
		implement();
	}
	private void conExpression() {
		arithExpression();
		operation();
		arithExpression();
	}
	private void operation() {
		if(currToken.type>=13 && currToken.type <=17) {
			match();
			return;//<>  <=  <  >= >		
		}else error(13);
	}
	private boolean isVariDefined(String name) {
		for(int i =0;i < variTab.size();i++) {
			if(variTab.get(i).vName.equals(name))
				return true;
		}
		return false;
	}
	private boolean isProDefined(String name) {//to check whether this process has been defined
		for(int i=0 ; i < proTab.size(); i++) {
			if(proTab.get(i).pName.equals(name))return true;
		}
		return false;
	}
	private boolean fillInVar(String name,int kind) {// 0: variate    1:parameter
		Vari vari = new Vari();
		vari.vName = name; vari.vKind = kind; vari.vType = "integer";
		//check the process table to find the process that it belongs to
		for(int i = proTab.size()-1;i >= 0; i--) {
			if(!proTab.get(i).isFinished) {
				vari.vProc = proTab.get(i).pName;
				vari.vLev = proTab.get(i).pLev;
				break;
			}
		}
		//check the variate table whether it has been defined
		for(int i=0;i < variTab.size();i++) {
			if(variTab.get(i).vName.equals(vari.vName)) {//the same names
				if(variTab.get(i).vProc.equals(vari.vProc))//belongs to a same process
					return false;
			}
		}
		//add it to the table
		variTab.add(vari);
		return true;
	}
	private void finishPro(String name) {//a process has been defined completely
		process  pro = new process();
		pro.isFinished = true;
		pro.pName = name;
		for(int i = proTab.size()-1;i >= 0; i--) {
			if(proTab.get(i).pName.equals(pro.pName)) {
				pro.pLev = proTab.get(i).pLev;
				pro.pType = proTab.get(i).pType;
				proTab.set(i, pro);
				break;
			}
		}
	}
	private boolean fillInPro(String name, String type) {//flase means defined reduplicative
		process pro = new process(name,type,0);
		if(proTab.size() == 0) {
			proTab.add(pro); return true;
		}
		boolean founded = false;
		for(int i = proTab.size()-1;i >= 0; i--) {
			if(!proTab.get(i).isFinished && !founded) {
				pro.pLev = proTab.get(i).pLev + 1;
				founded =true;
			}
			if(proTab.get(i).pName.equals(name)) return false;//has been defined
		}
		proTab.add(pro);
		return true;
	}
	private void match() {
		if(pointer >= tokens.size()) return;
		currToken = tokens.get(++pointer);
		if(currToken.type == 24) {
			errLine++;
			match();			
		}
			
	}
	private int lookAhead(int num) {
		int result = tokens.get(tokens.size()-1).type;
		if (pointer + num >= tokens.size()) return result;
		else {
			int i = pointer + 1;
			for(int k = pointer +num; i <= k ;i++) {
				result = tokens.get(i).type;
				if(result == 24) {     //another line
					if(++k >= tokens.size())return tokens.get(tokens.size()-1).type;
				}
			}
			return result;
		}
	}
	private void adavance(int expect) {
		if(currToken.type == expect)
			match();
		else error(expect);			
	}
	public void error(int errType) {
		String errMessage = "<ERROR>LINE:  "+ errLine + " ";
		switch (errType) {
		case 1:
			errMessage += "Missing a 'begin'";
			break;
		case 2:
			errMessage += "Missing an 'end'";
			break;
		case 3:
			errMessage += "Missing an 'integer'";
			break;	
		case 4:
			errMessage += "Missing a 'if'";
			break;	
		case 5:
			errMessage += "Missing an 'then'";
			break;	
		case 6:
			errMessage += "Missing a 'else'";
			break;	
		case 7:
			errMessage += "Missing a 'function'";
			break;
		case 8:
			errMessage += "Missing a 'read'";
			break;
		case 9:
			errMessage += "Missing a 'write'";
			break;
		case 10:
			errMessage += "Missing an identifier";
			break;
		case 11:
			errMessage += "Missing a constant number";
			break;
		case 12:
			errMessage += "Missing a '='";
			break;
		case 13:
			errMessage += "Missing an 'operation'";
			break;
		case 18:
			errMessage += "Missing a '-'";
			break;
		case 19:
			errMessage += "Missing a '*'";
			break;
		case 20:
			errMessage += "Missing a ':='";
			break;
		case 21:
			errMessage += "Missing a '('";
			break;
		case 22:
			errMessage += "Missing a ')'";
			break;
		case 23:
			errMessage += "Missing a ';'";
			break;
		case 24:
			errMessage += "The variable '"+ currToken.character +"' has already been defined";
			break;
		case 25:
			errMessage += "The function '"+ currToken.character +"' has already been defined";
			break;
		case 26:
			errMessage += "No main function";
			break;
		case 27:
			errMessage += "The symbol '"+ currToken.character +"' has not been defined";
			break;
		case 28:
			errMessage += "Missing an implement sentence";
			break;
		default:
			errMessage += "Unkonewn error";
			break;
		}
		errTab.add(errMessage);
		//match();
	}
	private void init() {
		File dydFile = new File(dydPath);
		try (BufferedReader reader = new BufferedReader(new FileReader(dydFile))){
			String line = reader.readLine();
			while(line != null) {
				Token tmpToken = new Token();
				String data[] = line.split("\\s+");
//				if(data[data.length - 1].equals("25")) break;
				tmpToken.character = data[data.length -2];
				try {
					tmpToken.type = Integer.parseInt(data[data.length - 1]);
				}catch (NumberFormatException e) {e.printStackTrace();}
				tokens.add(tmpToken);
				if(data[data.length - 1].equals("25")) break;
				line = reader.readLine();
			}
		}catch (IOException e) {e.printStackTrace();}
	   File errFile = new File(errPath);
	   File proFile = new File(proPath);
	   File varFile = new File(varPath);
	   File dysFile = new File(dysPath);
	   try {
		   if(!errFile.exists())errFile.createNewFile();
		   if(!proFile.exists())proFile.createNewFile();
		   if(!varFile.exists())varFile.createNewFile();
		   if(!dysFile.exists())varFile.createNewFile();
	   }catch (IOException e) {e.printStackTrace();}
	   errLine = 1;
	   pointer = 0;
	   currToken = tokens.get(0);	
	   if(currToken.type==24) {
		   errLine++;
		   match();		   
		  }
	}
}

class Token{
	public String character;
	public int type;
} 
class process{
	 public String pName;
	 public String pType;
	 public int pLev;
	 public boolean isFinished;// has it be defined compeletely or not
	 process() {
		 this.isFinished = false;
	 }
	process(String pname,String ptype,int plev){
		this.pName = pname;
		this.pType = ptype;
		this.pLev = plev;
		this.isFinished = false;
	}
}

class Vari{
	public String vName;//   name of variate
	public String vProc;//   process it belongs to
	public int vKind;// 0: variate    1:parameter
	public String vType;//    integer
	public int vLev;//       level of it
}