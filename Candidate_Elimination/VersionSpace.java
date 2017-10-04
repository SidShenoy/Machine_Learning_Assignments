import java.io.*;	// -2 corresponds to "none" and -1 corresponds to "?"

public class VersionSpace {
	int gBoundary[][] = new int[100001][16];
	int sBoundary[][] = new int[100001][16];
	int classification, countG, countS;
	
	VersionSpace(int i)
	{
		classification = i;	
		
		for(int j=0;j<16;j++)
		{
			gBoundary[0][j] = -1;
			sBoundary[0][j] = -2;
		}
		
		countG = 1;
		
		countS = 1;
	}
	
	void add_to_S(int s[])
	{
		int i = 0, k = 0;
		
		while(i<countS)
		{
			if(sBoundary[k][0] != -3)
				i++;
			else
				break;
			
			k++;
		}
		
		for(int j=0;j<16;j++)
			sBoundary[k][j] = s[j];
		
		countS++;
	}
	
	void add_to_G(int g[])
	{
		int i = 0,k=0;
		
		while(i<countG)
		{	
			if(gBoundary[k][0] != -3)
				i++;
			else
			{
				break;
			}
			
			k++;
		}
		
		for(int j=0;j<16;j++)
			gBoundary[k][j] = g[j];
		
		countG++;
	}
	
	int does_some_g_exist(int h[])
	{
		int j=0,k=0;
		
		while(j<countG)
		{
			int gen = 0;
			
			int test = 0;
			
			if(gBoundary[k][0]!=-3)
			{
				//int test = 0;
				for(int i=0;i<16;i++)
				{
					if(h[i]>=0 && gBoundary[k][i]==-1)
						gen++;
					else if(h[i]==-2 && gBoundary[k][i]>=-1)
						gen++;
					else if(h[i]==gBoundary[k][i])
						continue;
					else
					{
						test = 1;
						break;
					}
				}
				
				if(gen>=1 && test==0)
					return 1;
					
				j++;
			}
			
			k++;
		}
		
		return 0;
	}
	
	int does_some_s_exist(int h[])
	{
		int j=0,k=0;
		
		while(j<countS)
		{
			int spec = 0;
			
			int test = 0;
			
			if(sBoundary[k][0]!=-3)
			{
				for(int i=0;i<16;i++)
				{
					if(h[i]>=0 && sBoundary[k][i]==-2)
						spec++;
					else if(h[i]==-1 && ((sBoundary[k][i]>=0) || (sBoundary[k][i]==-2)))
						spec++;
					else if(h[i]==sBoundary[k][i])
						continue;
					else
					{
						test = 1;
						break;
					}
				}
				
				if(spec>=1 && test==0)
					return 1;
					
				j++;
			}
			
			k++;
		}
		
		return 0;
	}
	
	public int check(int h[],String example[])
	{
		for(int i=0;i<16;i++)
		{
			if(example[i].equals(""+h[i]) || h[i] == -1)
				continue;
			else
				return 0;
		}
		
		return 1;
	}
	
	public void updateS(String[] example, int i, int indexOfName)
	{
		if(i==0)
		{
			int j=0,k=0;
			
			while(j<countS)
			{
				int consistent;
				
				if(sBoundary[k][0]!=-3)
				{
					consistent = check(sBoundary[k],example);
					
					if(consistent==1)
					{
						sBoundary[k][0] = -3;
						countS--;
					}
					
					j++;
				}
				
				k++;
			}
		}
		else
		{
			int j=0,k=0,countBuffer=0;;
			int buffer[][] = new int[10000][16];
			
			while(j<countS)
			{
				int consistent;
				int newS[] = new int[16];
				
				if(sBoundary[k][0]!=-3)
				{
					consistent = check(sBoundary[k],example);
					
					if(consistent!=1)
					{
						for(int n=0;n<16;n++)
							newS[n] = sBoundary[k][n];
						
						for(int m=0;m<16;m++)
						{	
							if(sBoundary[k][m]==-2)
								{
									newS[m] = Integer.parseInt(example[m]);
								}
							else if(sBoundary[k][m]!=-1)
							{
								if(sBoundary[k][m]!=Integer.parseInt(example[m]))
									newS[m] = -1;
							}
						}
						
						if(check(newS,example)==1)
						{
							for(int n=0;n<16;n++)
							{
								buffer[countBuffer][n] = newS[n]; //add the new s hypothesis to the buffer
							}
							
							countBuffer++;
						}
						
						sBoundary[k][0] = -3;
						countS--;
					}
					
					j++;
				}
				
				k++;
			}
			
			//remove all those h present in buffer for whom a corresponding generic boundary does not exist
			for(j=0;j<countBuffer;j++)
			{
				if(does_some_g_exist(buffer[j])!=1)
				{
					buffer[j][0] = -3;
				}
			}
			
			//remove all those h present in buffer which are more general than some sBoundary
			for(j=0;j<countBuffer;j++)
			{	
				if((buffer[j][0]!=-3) && (does_some_s_exist(buffer[j])==1))
				{
					buffer[j][0] = -3;
				}
			}
			
			//add the remaining buffer to sBoundary
			for(j=0;j<countBuffer;j++)
			{
				if(buffer[j][0]!=-3)
				{
					add_to_S(buffer[j]);
				}
			}
			
		}
	}
	
	public void updateG(String[] example, int i,int indexOfName)
	{
		if(i==0)
		{
			int j=0,k=0;
			
			while(j<countG)
			{
				int consistent;
				
				if(gBoundary[k][0]!=-3)
				{
					consistent = check(gBoundary[k],example);
					
					if(consistent!=1)
					{
						gBoundary[k][0] = -3;
						countG--;
					}
					
					j++;
				}
				
				k++;
			}
		}
		else
		{
			int j=0,k=0,countBuffer=0;
			int buffer[][] = new int[10000][16];
			
			while(j<countG)
			{
				int consistent;
				
				if(gBoundary[k][0]!=-3)
				{
					consistent = check(gBoundary[k],example);
					
					if(consistent==1)	//if consistent, we have to modify it and make it inconsistent
					{
						for(int m=0;m<16;m++)
						{		
							int newG[] = new int[16];
							for(int n=0;n<16;n++)
								newG[n] = gBoundary[k][n];
							
							if(gBoundary[k][m]==-1)
								{
										if(m!=12)
										{
											newG[m] = (Integer.parseInt(example[m]) + 1)%2;
											
											for(int n=0;n<16;n++)
											{
												buffer[countBuffer][n] = newG[n]; //add the new s hypothesis to the buffer
											}
											
											countBuffer++;
										}
										else
										{
											int legs[] = {0,2,4,5,6,8};
											
											for(int p = 0;p<=5;p++)
											{
												if(Integer.parseInt(example[12]) == legs[p])
												continue;
												
												for(int n=0;n<16;n++)
													newG[n] = gBoundary[k][n];
												
												newG[m] = legs[p];
												
												for(int n=0;n<16;n++)
												{
													buffer[countBuffer][n] = newG[n]; //add the new s hypothesis to the buffer
												}
												
												countBuffer++;
											}
										}
									
								}
							if(sBoundary[k][m]>=0)
							{
								newG[m] = -2;
								
								for(int n=0;n<16;n++)
								{
									buffer[countBuffer][n] = newG[n]; //add the new s hypothesis to the buffer
								}
								
								countBuffer++;
							}
						}
						
						gBoundary[k][0] = -3;
						countG--;
					}
					
					j++;
				}
				
				k++;
				//complete this
			}
			
			//remove all those h present in buffer for whom a corresponding specific boundary does not exist
			for(j=0;j<countBuffer;j++)
			{
				if(does_some_s_exist(buffer[j])!=1)
				{
					buffer[j][0] = -3;
				}
			}
			
			//remove all those h present in buffer which are more specific than some gBoundary
			for(j=0;j<countBuffer;j++)
			{	
				if((buffer[j][0]!=-3) && (does_some_g_exist(buffer[j])==1))
				{
					buffer[j][0] = -3;
				}
			}
			
			//add the remaining buffer to sBoundary
			for(j=0;j<countBuffer;j++)
			{
				if(buffer[j][0]!=-3)
				{	
					add_to_G(buffer[j]);
				}
			}
		}
	}
	
	public static void main(String args[]) throws Exception
	{
		int i = 1;
		VersionSpace v[] = new VersionSpace[8];
		BufferedReader br;
		
		File f = new File("zoo.data.txt");
		
		for(int j=1;j<=7;j++)
			v[j] = new VersionSpace(j);
		
		
			while(i<=7)
			{
				String t_example;
					
				br = new BufferedReader(new FileReader(f));
				
				int count = 0;
				
				while((t_example=br.readLine())!=null)
				{
					count++;
					
					String current_example_from_file[] = t_example.split(",");
					String current_example[] = new String[16];
					
					for(int y=1;y<17;y++)
						current_example[y-1] = current_example_from_file[y];
					
					int length = current_example_from_file.length;
					
					if(current_example_from_file[length-1].equals(""+v[i].classification)) //for +ve example
					{
						v[i].updateG(current_example, 0, count);
						v[i].updateS(current_example, 1, count);
					}
					else														// for -ve example
					{
						v[i].updateS(current_example, 0, count);
						v[i].updateG(current_example, 1, count);
					}
				}
				
				//remaining code to print result
				
				br.close();
				
				i++;
			}
		
		
		System.out.println("Program successfully completed!!!");
		
		//printing for class 1-7
		
		for(int k=1;k<=7;k++)
		{
			int j=0,m=0;
			
			System.out.println("Specific boundaries for class "+v[k].classification+"(countS="+v[k].countS+") :");
			
			while(j<v[k].countS)
			{
				if(v[k].sBoundary[m][0]!=-3)
				{
					for(int n=0;n<16;n++)
						System.out.print(v[k].sBoundary[m][n]+", ");
					
					System.out.println();
					
					j++;
				}
				
				m++;
			}
		}
		
		System.out.println("***************************************");
		
		for(int k=1;k<=7;k++)	//remove those g boundaries that don't have a corresponding s boundary.
		{
			int j=0,m=0;		
							
			while(j<v[k].countG)
			{
				if(v[k].gBoundary[m][0]!=-3)
				{
					if(v[k].does_some_s_exist(v[k].gBoundary[m])==0)
					{
						v[k].gBoundary[m][0] = -3;
						v[k].countG--;
					}
					
					j++;
				}
				
				m++;
			}
		}
		
		for(int k=1;k<=7;k++)
		{
			int j=0,m=0;
			
			System.out.println("Generic boundaries for class "+v[k].classification+"(countG="+v[k].countG+") :");
			
			while(j<v[k].countG)
			{
				if(v[k].gBoundary[m][0]!=-3)
				{
					for(int n=0;n<16;n++)
						System.out.print(v[k].gBoundary[m][n]+", ");
					
					System.out.println();
					
					j++;
				}
				
				m++;
			}
		}
	}
}
