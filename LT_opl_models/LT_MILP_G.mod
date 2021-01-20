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
float ft = ...;
float ut = ...;

float initialStock[1..2] = ...;

int   nbpartitions=...;
range partitions=1..nbpartitions;
float prob[partitions]=...;
float lamda_matrix[t in months, j in months, i in partitions] = ...;

//decision variables
dvar float stock1[0..nbmonths];
dvar float+ stockhlb1[0..nbmonths];
dvar float+ stockplb1[0..nbmonths];
dvar float stock2[0..nbmonths];
dvar float+ stockhlb2[0..nbmonths];
dvar float+ stockplb2[0..nbmonths];

dvar float+ Q1[months];
dvar float+ Q2[months];
dvar boolean purchase1[months];
dvar boolean purchase2[months];

dvar float transship[months];
dvar boolean transshipDecision[months];

dvar float+ U1[months];
dvar float+ U2[months];
dvar float W[months];

dvar boolean P1[months][months];
dvar boolean P2[months][months];

float mean_matrix1[i in months, j in months] = sum(m in i..j) meandemand1[m];
float mean_matrix2[i in months, j in months] = sum(m in i..j) meandemand2[m];

//objective function
minimize sum(t in months)(
							purchase1[t]*fc+v*U1[t] +
							purchase2[t]*fc+v*U2[t] +
							transshipDecision[t]*ft + ut*abs(W[t]) + 
							h*stockhlb1[t]+p*stockplb1[t] + 
							h*stockhlb2[t]+p*stockplb2[t]
);
//constraints
subject to{

 stock1[0] == initialStock[1];
 stockhlb1[0]==maxl(stock1[0],0);
 stockplb1[0]==maxl(-stock1[0],0);
 
 stock2[0] == initialStock[2];
 stockhlb2[0]==maxl(stock2[0],0);
 stockplb2[0]==maxl(-stock2[0],0);

//transform
forall(t in months) purchase1[t] == 1 => U1[t]==Q1[t];
forall(t in months) purchase1[t] == 0 => U1[t]==0;
forall(t in months) purchase2[t] == 1 => U2[t]==Q2[t];
forall(t in months) purchase2[t] == 0 => U2[t]==0;
forall(t in months) transshipDecision[t] == 1 => W[t] == transship[t];
forall(t in months) transshipDecision[t] == 0 => W[t] == 0;

forall(t in months) purchase1[t] ==0 => Q1[t] == 0;
forall(t in months) purchase2[t] ==0 => Q2[t] == 0;
forall(t in months) transshipDecision[t] == 0 => transship[t] ==0;

//--------------------Gn-------------------  		
purchase1[1] == 0;
purchase2[1] == 0;
transshipDecision[1] == 0;
 
 //flow balance family (8 constraints)
 //location 1
//forall(t in months) stock1[t-1] - transshipDecision[t]*transship[t] + purchase1[t]*Q1[t] - meandemand1[t] == stock1[t];
//location 2
//forall(t in months) stock2[t-1] + transshipDecision[t]*transship[t] + purchase2[t]*Q2[t] - meandemand2[t] == stock2[t];

//flow balances(transship, order for 1, order for 2)
//transship
forall(t in months) (transshipDecision[t] == 1) && (purchase1[t] == 1) => stock1[t-1] - transship[t] + Q1[t] - meandemand1[t] == stock1[t];
forall(t in months) (transshipDecision[t] == 1) && (purchase2[t] == 1) => stock2[t-1] + transship[t] + Q2[t] - meandemand2[t] == stock2[t];
forall(t in months) (transshipDecision[t] == 1) && (purchase1[t] == 0) => stock1[t-1] - transship[t] - meandemand1[t] == stock1[t];
forall(t in months) (transshipDecision[t] == 1) && (purchase2[t] == 0) => stock2[t-1] + transship[t] - meandemand2[t] == stock2[t];
//no transship
forall(t in months) (transshipDecision[t] == 0) && (purchase1[t] == 1) => stock1[t-1] + Q1[t] - meandemand1[t] == stock1[t];
forall(t in months) (transshipDecision[t] == 0) && (purchase2[t] == 1) => stock2[t-1] + Q2[t] - meandemand2[t] == stock2[t];
forall(t in months) (transshipDecision[t] == 0) && (purchase1[t] == 0) => stock1[t-1] - meandemand1[t] == stock1[t];
forall(t in months) (transshipDecision[t] == 0) && (purchase2[t] == 0) => stock2[t-1] - meandemand2[t] == stock2[t];

//transshipment domain
forall(t in months) transship[t] <= maxl(stock1[t-1],0);
forall(t in months) transship[t] >= minl(-stock2[t-1],0);

//P sum
forall (t in months)
  		sum(j in 1..t)P1[j][t]==1;
 forall (t in months)
  		sum(j in 1..t)P2[j][t]==1;

//unique replenishment  		
forall (t in months, j in 1..t)
  		P1[j][t]>=purchase1[j]-sum(k in j+1..t)purchase1[k];
forall (t in months, j in 1..t)
  		P2[j][t]>=purchase2[j]-sum(k in j+1..t)purchase2[k];   		
forall (t in months)
   		sum(k in 1..t) purchase1[k] == 0 => P1[1][t] == 1;  
forall (t in months)
   		sum(k in 1..t) purchase2[k] == 0 => P2[1][t] == 1;  
   		
   
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



	
