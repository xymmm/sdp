/*********************************************
 * OPL 12.8.0.0 Model
 * Author: Xiyua
 * Creation Date: Nov 4, 2019 at 3:55:33 PM
 *********************************************/

//parameters
int nbmonths=...;
range months=1..nbmonths;
float fc=...;
float h=...;
float p=...;
float v = ...;
int meandemand[months]=...;

int initialStock = ...;

int nbpartitions=...;
range partitions=1..nbpartitions;
float prob[partitions]=...;
float lamda_matrix[t in months, j in months, i in partitions] = ...;

//variables
dvar float stock[0..nbmonths];
dvar float+ stockhlb[0..nbmonths];
dvar float+ stockplb[0..nbmonths];
dvar boolean purchase[months];
dvar boolean P[months][months];
dvar float+ U[1..nbmonths];

dvar float+ Q[months];

float mean_matrix[i in months, j in months] = sum(m in i..j) meandemand[m];

//objective function
minimize sum(t in months)( fc*purchase[t]+h*stockhlb[t]+p*stockplb[t] +  v*U[t]);

//constraints
subject to{
 
 purchase[1] == 0;

 stock[0] == initialStock;
 stockhlb[0]==maxl(stock[0],0);
 stockplb[0]==maxl(-stock[0],0);

forall(t in months) purchase[t] == 0 => U[t] == 0;
forall(t in months) purchase[t] == 1 => U[t] == Q[t];

forall(t in months) purchase[t] == 0 => stock[t] + meandemand[t] - stock[t-1] == 0;
forall(t in months) purchase[t] == 1 => stock[t] + meandemand[t] - stock[t-1] == Q[t]; 

forall (t in months)
  		sum(j in 1..t)P[j][t]==1;
 
forall (t in months, j in 1..t)
  		P[j][t]>=purchase[j]-sum(k in j+1..t)purchase[k];
  
forall (t in months)
   		sum(k in 1..t) purchase[k] == 0 => P[1][t] == 1;  

forall(t in months) Q[t] <= 9;
   
/** Original formulation for Poisson holding cost / complementary loss **/
forall(t in months, p in partitions)
  stockhlb[t] >= (sum(k in 1..p)prob[k]) * (stock[t] + sum(j in 1..t)P[j][t]*mean_matrix[j][t])
  					- sum(j in 1..t)(P[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]*prob[k]));
//forall(t in months, p in partitions) stockhlb[t] >= sum(j in 1..t) error[j][t][p] * P[j][t];
    
    
/** Original formulation for Poisson penalty cost / loss  **/ 
forall(t in months, p in partitions)
  stockplb[t] >= -stock[t] + (sum(k in 1..p)prob[k]) * (stock[t] + sum(j in 1..t)P[j][t]*mean_matrix[j][t])
  					- sum(j in 1..t)(P[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]*prob[k]));
//forall(t in months, p in partitions) stockplb[t] >= -stock[t] + sum(j in 1..t) error[j][t][p] * P[j][t];  

}
 