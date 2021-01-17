/*********************************************
 * OPL 12.9.0.0 Model
 * Author: Xiyuan
 * Creation Date: 2021年1月17日 at 下午2:41:05
 *********************************************/
//parameters
int nbmonths=...;
range months=1..nbmonths;
float fc=...;
float h=...;
float p=...;
float v = ...;
float meandemand1[months]=...;
float meandemand2[months]=...;

float initialStock[1..2] = ...;

int   nbpartitions=...;
range partitions=1..nbpartitions;
float prob[partitions]=...;
float lamda_matrix[t in months, j in months, i in partitions] = ...;

//variables
dvar float stock1[0..nbmonths];
dvar float+ stockhlb1[0..nbmonths];
dvar float+ stockplb1[0..nbmonths];
dvar boolean purchase1[months];
dvar float+ U1[1..nbmonths];
dvar boolean P1[months][months];
dvar float stock2[0..nbmonths];
dvar float+ stockhlb2[0..nbmonths];
dvar float+ stockplb2[0..nbmonths];
dvar boolean purchase2[months];
dvar float+ U2[1..nbmonths];
dvar boolean P2[months][months];
//dvar float transship[months];
//dvar boolean transshipDecision[months];

dvar float+ Q1[months];
dvar float+ Q2[months];
dvar float+ initialOrder[1..2];


float mean_matrix1[i in months, j in months] = sum(m in i..j) meandemand1[m];
float mean_matrix2[i in months, j in months] = sum(m in i..j) meandemand2[m];

//objective function
minimize sum(t in months)( fc*purchase1[t]+h*stockhlb1[t]+p*stockplb1[t] + v*Q1[t]
							+ fc*purchase2[t]+h*stockhlb2[t]+p*stockplb2[t] + v*Q2[t]
);
//constraints
subject to{

 stock1[0] == initialStock[1];
 stockhlb1[0]==maxl(stock1[0],0);
 stockplb1[0]==maxl(-stock1[0],0);
 stock2[0] == initialStock[2];
 stockhlb2[0]==maxl(stock2[0],0);
 stockplb2[0]==maxl(-stock2[0],0);

//forall(t in months) purchase1[t] == 1 => U1[t]==Q1[t];
//forall(t in months) purchase1[t] == 0 => U1[t]==0;
//forall(t in months) purchase2[t] == 1 => U2[t]==Q2[t];
//forall(t in months) purchase2[t] == 0 => U2[t]==0;

forall(t in months) purchase1[t] == 0 => stock1[t] + meandemand1[t] - stock1[t-1] == 0;
forall(t in months) purchase1[t] == 1 => stock1[t] + meandemand1[t] - stock1[t-1] == Q1[t]; 
forall(t in months) purchase2[t] == 0 => stock2[t] + meandemand2[t] - stock2[t-1] == 0;
forall(t in months) purchase2[t] == 1 => stock2[t] + meandemand2[t] - stock2[t-1] == Q2[t]; 

initialOrder[1] == Q1[1];
initialOrder[2] == Q2[1];

forall (t in months)
  		sum(j in 1..t)P1[j][t]==1;
 forall (t in months)
  		sum(j in 1..t)P2[j][t]==1;
 
forall (t in months, j in 1..t)
  		P1[j][t]>=purchase1[j]-sum(k in j+1..t)purchase1[k];
forall (t in months, j in 1..t)
  		P2[j][t]>=purchase2[j]-sum(k in j+1..t)purchase2[k]; 
  		
forall (t in months)
   		sum(k in 1..t) purchase1[k] == 0 => P1[1][t] == 1;  
forall (t in months)
   		sum(k in 1..t) purchase2[k] == 0 => P2[1][t] == 1;  
   		
//to compute s, predefine the order decision in first period   		
//purchase[1] == 0;
   
/** Original formulation for Poisson holding cost / complementary loss **/
forall(t in months, p in partitions)
  stockhlb1[t] >= (sum(k in 1..p)prob[k]) * (stock1[t] + sum(j in 1..t)(P1[j][t] * mean_matrix1[j][t]))
  					- sum(j in 1..t)(P1[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]*prob[k]));
//forall(t in months, p in partitions) stockhlb[t] >= sum(j in 1..t) error[j][t][p] * P[j][t];
forall(t in months, p in partitions)
  stockhlb2[t] >= (sum(k in 1..p)prob[k]) * (stock2[t] + sum(j in 1..t)(P2[j][t] * mean_matrix2[j][t]))
  					- sum(j in 1..t)(P2[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]*prob[k]));

    
/** Original formulation for Poisson penalty cost / loss  */ 
forall(t in months, p in partitions)
  stockplb1[t] >= -stock1[t] + (sum(k in 1..p)prob[k]) * (stock1[t] + sum(j in 1..t)(P1[j][t] * mean_matrix1[j][t]))
  					- sum(j in 1..t)(P1[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]*prob[k]));
//forall(t in months, p in partitions) stockplb[t] >= -stock[t] + sum(j in 1..t) error[j][t][p] * P[j][t];  
forall(t in months, p in partitions)
  stockplb2[t] >= -stock2[t] + (sum(k in 1..p)prob[k]) * (stock2[t] + sum(j in 1..t)(P2[j][t] * mean_matrix2[j][t]))
  					- sum(j in 1..t)(P2[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]*prob[k]));

}
/*
main{
	var ofile1 = new IloOplOutputFile("E:\\temp.txt");
		
	for(var i in thisOplModel.inventory){	
		var mod = thisOplModel.modelDefinition;
		var dat = thisOplModel.dataElements;
		var MyCplex = new IloCplex(); 
		var opl = new IloOplModel(mod, MyCplex);		
		opl.addDataSource(dat);
		dat.initialStock = i;
		opl.generate();	
		opl.settings.mainEndEnabled = true;		
		if(MyCplex.solve()){
			ofile1.writeln(MyCplex.getObjValue(), "\t",i);	
		}		
	}

	opl.end();
	MyCplex.end;
	dat.end();
	mod.end();

	ofile1.close();

	
}*/