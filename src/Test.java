public class Test {
	
	int foo(int x, int y, int z)
	{
		int a = x;
		int b = a + y;
		int c = b + z + bar(12);
		
		System.out.print(a+x-c);
		System.out.print(b+2-a);
		System.out.print(c/b);
		
		x = 10;
		a = x;
		
		if (a > 10) {
			while(b > 10) {
				b--;
				System.out.print(c);
			}
			b = 10 - bar(16);
			System.out.print(z);
		}
		else {
			while(c > 10) {
				c--;
				System.out.print(b);
			}
			b = 12;
			System.out.print(z);
		}
		
		z = b + bar(10);
		
		System.out.println(b + z + " " + 16);
		
		return z;
	}
	
	int bar(int x) {
		x = 12;
		return x;
	}
}