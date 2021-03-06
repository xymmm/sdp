/*********************************************
 * OPL 12.8.0.0 Model
 * Author: Xiyuan
 * Creation Date: Sep 23, 2019 at 1:08:34 PM
 *********************************************/

//parameters
int nbmonths=...;
range months=1..nbmonths;
float fc=...;
float h=...;
float p=...;
float v = ...;
float meandemand[months]=...;
float stdParameter=...;

float initialStock = ...;

int nbpartitions=...;
range partitions=1..nbpartitions;
float means[partitions]=...;
float prob[partitions]=...;
float error=...;
//int maxOrderQty = 9;

//variables
dvar float stock[0..nbmonths];
dvar float+ stockhlb[0..nbmonths];
dvar float+ stockplb[0..nbmonths];
dvar boolean purchase[months];

dvar float purchaseDouble[months];

dvar boolean P[months][months];
dvar float+ U[1..nbmonths];

dvar float+ Q;

float std_demand[i in months] = stdParameter * meandemand[i];
//float mean_matrix[i in months, j in months] = sum(m in i..j) meandemand[m];
float std_matrix[i in months, j in months] = sqrt(sum(m in i..j) pow(std_demand[m],2));

//objective function
minimize sum(t in months)( fc*purchase[t]+h*stockhlb[t]+p*stockplb[t] +  v*U[t]);

//constraints
subject to{
 stock[0] == initialStock;
 stockhlb[0]==maxl(stock[0],0);
 stockplb[0]==maxl(-stock[0],0);

forall(t in months) purchase[t] == 1 => U[t]==Q;
forall(t in months) purchase[t] == 0 => U[t]==0;

forall(t in months) purchase[t] == 1 => purchaseDouble[t] == 1;
forall(t in months) purchase[t] == 0 => purchaseDouble[t] == 0;

forall(t in months) purchase[t] == 0 => stock[t] + meandemand[t] - stock[t-1] == 0;
forall(t in months) purchase[t] == 1 => stock[t] + meandemand[t] - stock[t-1] == Q; 

forall (t in months)
  		sum(j in 1..t)P[j][t]==1;
 
forall (t in months, j in 1..t)
  		P[j][t]>=purchase[j]-sum(k in j+1..t)purchase[k];
  
forall (t in months)
   		sum(k in 1..t) purchase[k] == 0 => P[1][t] == 1;  
  
/**
* Original formulation as in (Rossi et al., 2015)    
*/ 
forall(t in months, p in partitions)
  stockhlb[t]>=sum(k in 1..p)prob[k]*stock[t]-sum(j in 1..t)(sum(k in 1..p)prob[k]*means[k]*std_matrix[j][t]*P[j][t]) + (sum(j in 1..t) error*std_matrix[j][t]*P[j][t]);

  forall(t in months) stockhlb[t] >= (sum(j in 1..t) error*std_matrix[j][t]*P[j][t]);
    
/**
* Original formulation as in (Rossi et al., 2015)    
*/ 
forall(t in months, p in partitions)
  stockplb[t]>=-stock[t]+sum(k in 1..p)prob[k]*stock[t]-sum(j in 1..t)(sum(k in 1..p)prob[k]*means[k]*std_matrix[j][t]*P[j][t]) + (sum(j in 1..t) error*std_matrix[j][t]*P[j][t]);
 
  forall(t in months) stockplb[t] >= - stock[t] + (sum(j in 1..t) error*std_matrix[j][t]*P[j][t]);

}
