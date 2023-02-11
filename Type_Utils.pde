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
  public boolean equals_strict(Pair other)
  {
    return (other.y == x && other.x==y);
  }
}


boolean ArrayListPairContains(ArrayList<Pair> HS, Pair other, boolean can_be_reverse)//is there really no lookup function for ArrayLists in Java? Am I missing something obvious?
{
  for (int i = 0; i<HS.size(); i++)
  {
    if (can_be_reverse && HS.get(i).equals(other))
    {
      return true;
    }
    if (!can_be_reverse && HS.get(i).equals_strict(other))
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
