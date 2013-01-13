package pt.cagojati.bombahman;

public interface IPowerUp {
	
	public float getX();
	public float getY();
	public void setX(float x);
	public void setY(float y);
	public void show(float posX, float posY);
	public int getType();
	public void apply(Player player);
	public void destroy();
}
