/*********************************************
 * OPL 12.8.0.0 Model
 * Author: Xiyuan
 * Creation Date: Sep 23, 2019 at 1:08:34 PM
 *********************************************/

int nbmonths=...;
range months=1..nbmonths;
float fc=...;
float h=...;
float p=...;
float meandemand[months]=...;

int minInventory = ...;
int maxInventory = ...;

int initialStock = 0;

int nbpartitions=...;
range partitions=1..nbpartitions;
float means[partitions]=...;
float prob[partitions]=...;
//float error=...;
float lamda_matrix[t in months, j in months, i in partitions] = ...;
float error[t in months, j in months, i in partitions] = ...;


//variables
dvar float stock[0..nbmonths];
dvar float+ stockhlb[0..nbmonths];
dvar float+ stockplb[0..nbmonths];
dvar boolean purchase[months];
dvar boolean P[months][months];

dvar float Q;


float mean_matrix[i in months, j in months] = sum(m in i..j) meandemand[m];

//objective function
minimize sum(t in months)( fc*purchase[t]+h*stockhlb[t]+p*stockplb[t]);

//constraints
subject to{

 stock[0] == initialStock;
 stockhlb[0]==maxl(stock[0],0);
 stockplb[0]==maxl(-stock[0],0);
 
 Q>=0;

 forall(t in months) {
   		purchase[t] == 0 => stock[t]+meandemand[t]-stock[t-1] == 0;     
   		purchase[t] == 1 => stock[t] + meandemand[t] - stock[t-1] == Q;
   }
 
 forall(t in months){
 	stock[t] >= minInventory;
 	stock[t] <= maxInventory; 
 }

forall (t in months)
  		sum(j in 1..t)P[j][t]==1;
 
forall (t in months, j in 1..t)
  		P[j][t]>=purchase[j]-sum(k in j+1..t)purchase[k];
  
forall (t in months)
   		sum(k in 1..t) purchase[k] == 0 => P[1][t] == 1;  
   
/** Original formulation for Poisson holding cost / complementary loss **/
forall(t in months, p in partitions)
  stockhlb[t] >= (sum(k in 1..p)prob[k]) * (stock[t] + sum(j in 1..t)(P[j][t] * mean_matrix[j][t]))
  					- sum(j in 1..t)(P[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]));
forall(t in months, p in partitions) stockhlb[t] >= sum(j in 1..t) error[j][t][p] * P[j][t];
    
    
/** Original formulation for Poisson penalty cost / loss  */ 
forall(t in months, p in partitions)
  stockplb[t] >= -stock[t] + (sum(k in 1..p)prob[k]) * (stock[t] + sum(j in 1..t)(P[j][t] * mean_matrix[j][t]))
  					- sum(j in 1..t)(P[j][t] * (sum(k in 1..p)lamda_matrix[j][t][p]));
forall(t in months, p in partitions) stockplb[t] >= -stock[t] + sum(j in 1..t) error[j][t][p] * P[j][t];  
  
  
/**
* Piecewise-based formulation (requires an even number of partitions: ftoi(round(nbpartitions/2)) )

forall(t in months, j in 1..t) 
  P[j][t] == 1 => stockhlb[t]/std_matrix[j][t] == 
  piecewise(i in partitions) {((sum(k in 1..i) prob[k]) - prob[i]) -> means[i]; 1} 
  (0, error - sum(k in 1..ftoi(round(nbpartitions/2)))(prob[k]*means[k])) 
  (stock[t]/std_matrix[j][t]);
*/  
 
/**
* Piecewise-based formulation (requires an even number of partitions: ftoi(round(nbpartitions/2)) )
  
forall(t in months, j in 1..t) 
  P[j][t] == 1 => stockplb[t]/std_matrix[j][t] == 
  piecewise(i in partitions) {(- 1 + (sum(k in 1..i) prob[k]) - prob[i]) -> means[i]; 0} 
  (0, error - sum(k in 1..ftoi(round(nbpartitions/2)))(prob[k]*means[k])) 
  (stock[t]/std_matrix[j][t]); 
*/

}
