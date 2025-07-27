public class Pair<X, Y> {//Override for equality checking. Definitely not optimized, but will help until I find a more elegant solution.
  public final X x;
  public final Y y;
  public Pair(X x, Y y) {
    this.x = x;
    this.y = y;
  }
  public boolean equals(Pair other)
  {
    return (other.x==x && other.y ==y) || (other.y == x && other.x==y);
  }
  public boolean equalsStrict(Pair other)
  {
    return (other.y == x && other.x==y);
  }
}


boolean ArrayListPairContains(ArrayList<Pair> HS, Pair other, boolean canBeReverse)//is there really no lookup function for ArrayLists in Java? Am I missing something obvious?
{
  for (int i = 0; i<HS.size(); i++)
  {
    if (canBeReverse && HS.get(i).equals(other))
    {
      return true;
    }
    if (!canBeReverse && HS.get(i).equalsStrict(other))
    {
      return true;
    }
  }
  return false;
}

int ArrayListPairGetAt(ArrayList<Pair> HS, Pair other)//indexof for tuples since ArrayList of complex structures are compared by reference and not values.
{
  for (int i = 0; i<HS.size(); i++)
  {
    if (HS.get(i).equals(other))
    {
      return i;
    }
  }
  return -1;
}
