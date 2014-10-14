package lexer;

import java.util.Vector;

public class Lexer {
 
  private String text;
  private Vector<Token> tokens; 
  private static final String[] KEYWORD = {"if", "else", "while", "switch", 
    "case", "return", "int", "float", "void", "char", "string", "boolean", 
    "true", "false", "print"};
  //Constants; YOU WILL NEED TO DEFINE MORE CONSTANTS
  private static final int ZERO      =  0;
  private static final int ONE       =  2;
  private static final int B         =  1;
  private static final int TWOSEVEN  =  3;
  private static final int EIGHTNINE =  4;
  private static final int XORX      =  5;
  private static final int ATOF      =  6;
  private static final int EORE      =  7;
  private static final int DOT       =  8;
  private static final int DOLLAR    =  9;
  private static final int UNDERSCORE= 10;
  private static final int GTOZ      = 11;
  private static final int PLUSMINUS = 12;
  private static final int BACKSLASH = 13;
  private static final int DOUBLEQUOTE=14;
  private static final int SINGLEQUOTE=15;
  private static final int OTHER     = 16;
  private static final int DELIMITER = 17;
  private static final int ERROR     = 21;
  private static final int STOP      = -2;
private static final int NA = 0;
  // states table; THIS IS THE TABLE FOR BINARY NUMBERS; YOU SHOLD COMPLETE IT
  private static final int[][] stateTable = { 
    {    1, 12,        5, 5,        5,  12,    12,    12,    8,     12,    12,    12, STOP, ERROR, 14,   18,  ERROR, STOP}, //s0
    {    4,     2,     4, 4,     ERROR, 6,     ERROR, 10,    9,     ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s1 
    {    3, ERROR,     3, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s2
    {    3, ERROR,     3, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s3-->binary
    {    4, ERROR,     4, 4,     ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s4-->octal
    {    5, ERROR,     5, 5,        5,  ERROR, ERROR, 10,    9,     ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s5-->Int
    {    7, 7,         7, 7,        7,  ERROR, 7,     7,     ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s6
    {    7, 7,         7, 7,        7,  ERROR, 7,     7,     ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s7-->Hex
    {    9, ERROR,     9, 9,        9,  ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s8
    {    9, ERROR,     9, 9,        9,  ERROR, ERROR, 10,    ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s9-->Float
    {   11, ERROR,    11, 11,      11,  ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,13,   ERROR, STOP, STOP,ERROR, STOP}, //s10
    {   11, ERROR,    11, 11,      11,  ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s11-->Float
    {   12, 12,       12, 12,      12,  12,    12,    12,    ERROR, 12,   12,   12,   STOP, ERROR, STOP, STOP,ERROR, STOP}, //s12-->Identifier
    {   11, ERROR,    11, 11,      11,  ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,STOP, ERROR, STOP, STOP,ERROR, STOP}, //s13-->intermediate for e+,e-
    {   15, 15,       15, 15,      15,  15,    15,    15,    15,    15,   15,   15,   15,   17,     16,  15,  15,    15  }, //s14-->after "
    {   15, 15,       15, 15,      15,  15,    15,    15,    15,    15,   15,   15,   15,   17,     16,  15,  15,    15  }, //s15
    {  NA, NA,       NA,  NA,     NA,   NA,    NA,    NA,    NA,    NA,   NA,  NA,   NA,    NA,     NA,  NA,   NA,   NA  }, //s16-->String
    {   15, 15,       15, 15,      15,  15,    15,    15,    15,    15,   15,   15,   15,   15,     15,  15,  15,    15  }, //s17--for \ in string
    {   19, 19,       19, 19,      19,  19,    19,    19,    19,    19,   19,   19,   19,   20,     19,  ERROR,19,   19  }, //s18-->after '
    {ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,ERROR,ERROR,ERROR, 22,  ERROR,ERROR}, //s19-->after '@
    {  19,  19,       19, 19,      19,  19,    19,    19,    19,    19,   19,   19,   19,   19,     19,  19,   19,   19  }, //s20-->for \ in character
    {ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,ERROR,ERROR,ERROR,ERROR, STOP, STOP,ERROR, STOP}  //sE
  };//zero    B     1     2-7     8-9    x|X    atof   E      /.      $     _    gtoz plus  backsl dquote squo other  delim
  
  //constructor
  public Lexer(String text) {
    this.text = text;
  }
  int count=0;
  int squotecount=0;
  int dquotecount=0;
  char prevChar=0;
  
  //run
  public void run () {
    tokens = new Vector<Token>();
    String line;
    int counterOfLines= 1;
    // split lines
    do {
      int eolAt = text.indexOf("\n");
      if (eolAt >= 0) {
        line = text.substring(0,eolAt); 
        if (text.length()>0) text = text.substring(eolAt+1);  
      } else {
        line = text;
        text = "";
      }
      splitLine (counterOfLines, line);
      counterOfLines++;
    } while ( !text.equals("") );   
  }
  
  //split line
  private void splitLine(int row, String line) {
    int state = 0;
    int index = 0;
    char currentChar;
    String string="";
    if (line.equals("")) return; 
    //DFA working
    int go; 
    do {   
      currentChar = line.charAt(index);
      go = calculateNextState(state, currentChar);
      if( go != STOP) {
        string = string + currentChar;   
        state = go;
      }
      

      index++;        
    } while (index < line.length() && go != STOP && go!=16 && go!=22);
    //review final state
    if (state == 3) {
      tokens.add(new Token(string, "BINARY", row));
    } 
    //for octal
    else if (state==4){tokens.add(new Token(string,"OCTAL",row));}
    //for integer
    else if (state==5||state==1){tokens.add(new Token(string,"INTEGER",row));}
    //for hexadecimal
    else if(state==7){tokens.add(new Token(string,"HEXADECIMAL",row));}
    //for float
    else if(state==9||state==11){tokens.add(new Token(string,"FLOAT",row));}
    //for identifiers and keywords
    else if(state==12)
    {
    	int count=0;
    	for(int i=0;i<KEYWORD.length;i++)
    		{
    		   if(string.equals(KEYWORD[i]))
    		      count+=1;
    		}
    	if(count>0)
    	  tokens.add(new Token(string,"KEYWORD",row));
    	else
    	  tokens.add(new Token(string,"IDENTIFIER",row));
    }
    //if state is 16 send line as substring of the next char
    else if(state==16)
    {
    	tokens.add(new Token(string,"STRING",row));
    	
    	if(index<line.length())
    	{
    	  line=line.substring(index);
    	  splitLine(row, line);
    	  line="";
    
    	} 	    		
    }
    //if state is 22 send line as substring of next char 
    else if(state==22)
    {
    	tokens.add(new Token(string,"CHARACTER",row));
    	
    	if(index<line.length())
    	{
    	  line=line.substring(index);
    	  splitLine(row, line);
    	  line="";
    
    	} 	   
    	
    }
    
   
   
    else {
      if (!string.equals(""))
        tokens.add(new Token(string, "ERROR", row));
    }
    // current char
    
    
    char nextChar=0;  
    String s1=new String();
    String s2=new String();
    if(index<line.length()&&line.charAt(index)!=-1)
    	nextChar=line.charAt(line.indexOf(currentChar)+1);

    // for checking double quote after a character
   if(currentChar=='"' && state!=16 && state!=0 &&state!=17 &&state!=15 && state!=14)
    {
	   
    	dquotecount++;
    	if(dquotecount%2!=0)
    	index=index-1;   	
    	
    }
   
   //for checking single quote after a character
   
   if(currentChar=='\'' && state!=0 && state!=18 && state!=19 && state!=20 && state!=22)
   {
	      if(!string.contains("''"))
	      squotecount++; 
	      
	      if(squotecount%2!=0)
		   index=index-1;
  
   }
    if( isDelimiter(currentChar) && state!=14 && state!=15 && state!=17 && state!=18 && state!=20)
        tokens.add(new Token(currentChar+"", "DELIMITER", row));
    
    else if (isOperator(currentChar) )
    {
      	
      if((currentChar=='='&&prevChar=='=')||(currentChar=='='&&prevChar=='!')||(currentChar=='!'&&prevChar=='=')) 
      {
    	
    	count++;
    	//System.out.println("prev char:" +prevChar);
    	//System.out.println("count is:" +count);
    	if(count%2==0)
    	 {
    		if(nextChar=='=')
    		 {s1=""+currentChar;
    		  s1=s1+""+nextChar;
    		  tokens.add(new Token(s1+"","OPERATOR",row));
    		 }
    		/*else if(nextChar=='!')
    		{
    			tokens.add(new Token(currentChar+"","OPERATOR",row));
    		    tokens.add(new Token(nextChar+"","OPERATOR",row));
    		}*/
    	   else
    		 tokens.add(new Token(currentChar+"","OPERATOR",row));
    	 }	
    	
    		
      }
      
      else if((currentChar=='='||currentChar=='!')&&nextChar=='=')
      {
    	  s2=""+currentChar;
    	  s2=s2+""+nextChar;
    	  tokens.add(new Token(s2+"","OPERATOR",row));
      }

      else
      { 
    	  
    	  tokens.add(new Token(currentChar+"", "OPERATOR", row));
      }
    }   //else if operator
    
    
    // loop
    prevChar=currentChar;
    System.out.println(prevChar);
   // System.out.println("I would like to know the value of line now:" +line);
    if (index < line.length()) 
      splitLine(row, line.substring(index));
    
  }
  
  
  // calculate state
  private int calculateNextState(int state, char currentChar) {
	
	if(currentChar=='+'||currentChar=='-')
		return stateTable[state][PLUSMINUS];
	 else if(currentChar=='"')
	    	return stateTable[state][DOUBLEQUOTE];
	 else if(currentChar=='\'')
		    return stateTable[state][SINGLEQUOTE];
	else if (isSpace(currentChar)  || isDelimiter(currentChar)  || 
      isOperator(currentChar) )	 
      return stateTable[state][DELIMITER];
	
    else if (currentChar == 'b') 
      return stateTable [state][B];
    else if (currentChar == '0')
      return stateTable [state][ZERO];    
    else if (currentChar == '1')
      return stateTable [state][ONE];
    else if(currentChar =='2'||currentChar=='3'||currentChar=='4'||currentChar=='5'||currentChar=='6'||currentChar=='7')
      return stateTable [state][TWOSEVEN];
    else if(currentChar=='8'||currentChar=='9')
      return stateTable [state][EIGHTNINE];	
    else if(currentChar=='x'||currentChar=='X')
    	return stateTable [state][XORX];
    else if(currentChar=='e'||currentChar=='E')
    	return stateTable [state][EORE];
    else if(currentChar=='a'||currentChar=='A'||currentChar=='B'||currentChar=='c'||currentChar=='C'||currentChar=='d'||currentChar=='D'||currentChar=='f'||currentChar=='F')
    	return stateTable [state][ATOF];
    else if(currentChar=='.')
    	return stateTable [state][DOT];
    else if(currentChar=='$')
    	return stateTable[state][DOLLAR];
    else if(currentChar=='_')
    	return stateTable[state][UNDERSCORE];
    else if(currentChar>='g' && currentChar<='z' || currentChar>='G' && currentChar<='Z' )
    	return stateTable[state][GTOZ];
    else if(currentChar=='\\')
    	return stateTable[state][BACKSLASH];
   
    else
    return stateTable [state][OTHER];
  }
 
  // isDelimiter
  private boolean isDelimiter(char c) {
     char [] delimiters = {':', ';', '}','{', '[',']','(',')',','};
     for (int x=0; x<delimiters.length; x++) {
      if (c == delimiters[x]) return true;      
     }
     return false;
  }
  
  // isOperator
  private boolean isOperator(char o) {
     // == and != should be handled in splitLine
     char [] operators = {'+', '-', '*','/','<','>','=','!','&','|'};
     for (int x=0; x<operators.length; x++) {
      if (o == operators[x]) return true;      
     }
     return false;
  }

  // isQuotationMark
 /* private boolean isQuotationMark(char o) {
     char [] quote = {'\''};
     for (int x=0; x<quote.length; x++) {
      if (o == quote[x]) return true;      
     }
     return false;
  }*/

  // isSpace
  private boolean isSpace(char o) {
     return o == ' ';
  }
  
  // getTokens
  public Vector<Token> getTokens() {
    return tokens;
  }
  
}
