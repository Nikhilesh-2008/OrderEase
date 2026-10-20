package model;

public class FoodItem {
    public int id;
    public String name;
    public double price;
    public boolean available;
    public String canteen;
    public String category;

    public FoodItem(int id, String name, double price) {
        this(id, name, price, true, "Snacks & Shakes Corner", "Fast Food");
    }

    public FoodItem(int id, String name, double price, boolean available) {
        this(id, name, price, available, "Snacks & Shakes Corner", "Fast Food");
    }

    public FoodItem(int id, String name, double price, boolean available, String canteen, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.available = available;
        this.canteen = (canteen != null && !canteen.trim().isEmpty()) ? canteen : "Snacks & Shakes Corner";
        this.category = (category != null && !category.trim().isEmpty()) ? category : "General";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCanteen() {
        return canteen;
    }

    public void setCanteen(String canteen) {
        this.canteen = canteen;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void display() {
        System.out.println(id + ". [" + canteen + "] " + name + " (" + category + ") - Rs." + price);
    }

    @Override
    public String toString() {
        return name + " - Rs." + String.format("%.2f", price);
    }
}