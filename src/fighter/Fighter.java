package fighter;

public sealed interface Fighter permits Enemy, Hero{
	void addProtection(int protectionPoints);
	void heal(int healPoints);
	void takeDamage(int damage);
	boolean isAlive(); 
}
