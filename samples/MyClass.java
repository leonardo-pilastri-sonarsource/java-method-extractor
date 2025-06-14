package samples;

public class MyClass {

  public void foo(){
    int i = 2 + 2;
    System.out.println("Int: " + i);
  }

  class Internal {
    private static int goo(int x){
      return x + 42;
    }
  }

}
