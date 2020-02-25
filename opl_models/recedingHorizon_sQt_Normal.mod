/*********************************************
 * OPL 12.9.0.0 Model
 * Author: Xiyuan
 * Creation Date: 2020年2月24日 at 下午9:08:15
 *********************************************/

//parameters
int nbmonths=...;
range months=1..nbmonths;
float fc=...;
float h=...;
float p=...;
float v = ...;
float demand[months]=...;

float initialStock = ...;

//variables
dvar float stock[0..nbmonths];
dvar float+ stockhlb[0..nbmonths];
dvar float+ stockplb[0..nbmonths];
dvar boolean purchase[months];
dvar boolean P[months][months];
dvar float+ U[1..nbmonths];

dvar float+ Q[months];

//objective function
minimize sum(t in months)( fc*purchase[t] + h*stockhlb[t] + p*stockplb[t] +  v*U[t]);

//constraints
subject to{
 stock[0] == initialStock;
 stockhlb[0]==maxl(stock[0],0);
 stockplb[0]==maxl(-stock[0],0);

forall(t in months) purchase[t] == 1 => U[t]==Q[t];
forall(t in months) purchase[t] == 0 => U[t]==0;

forall(t in months) purchase[t] == 0 => stock[t] + demand[t] - stock[t-1] == 0;
forall(t in months) purchase[t] == 1 => stock[t] + demand[t] - stock[t-1] == Q[t]; 

forall (t in months)
  		sum(j in 1..t)P[j][t]==1;
 
forall (t in months, j in 1..t)
  		P[j][t]>=purchase[j]-sum(k in j+1..t)purchase[k];
  
forall (t in months)
   		sum(k in 1..t) purchase[k] == 0 => P[1][t] == 1;  
   		
forall(t in months) stockhlb[t]==maxl(stock[t],0);
forall(t in months) stockplb[t]==maxl(-stock[t],0);
}
 