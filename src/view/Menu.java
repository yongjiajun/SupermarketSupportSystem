package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import model.pay.Sale;
import model.pay.SalesLineItem;
import model.people.Customer;
import model.people.Manager;
import model.people.SalesStaff;
import model.people.Supplier;
import model.people.User;
import model.people.WarehouseStaff;
import model.system.AccountManager;
import model.system.Product;
import model.system.ProductManager;
import model.system.SalesManager;

public class Menu {

	private AccountManager am;
	private ProductManager pm;
	private SalesManager sm;

	public Menu(AccountManager am, ProductManager pm, SalesManager sm) {
		this.am = am;
		this.pm = pm;
		this.sm = sm;
	}

	public void displayMainMenu() {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("\nWelcome to Kostko!");
		System.out.println("------------------");

		System.out.println("Please login to continue or type \"quit\" to quit:");
		System.out.print("\nUsername: ");
		String userName = sc.nextLine();
		if (userName.equalsIgnoreCase("quit")) {
			System.out.println("Good bye!\n");
			return;
		}
		System.out.print("Password: ");
		String pin = sc.nextLine();
		if (pin.equalsIgnoreCase("quit")) {
			System.out.println("Good bye!\n");
			return;
		}

		User user = am.verify(userName, pin);
		if (user == null || user instanceof SalesStaff) // as Supplier doesnt have to login!
		// and i've done salesStaff requirements dont worry
		{
			System.out.println("Login failed! Please try again.");
			sc.nextLine();
			displayMainMenu();
			return;
		}
		if (user instanceof Manager) {
			System.out.println("\nWelcome Manager " + user.getUserFName() + "\n");
			managerView((Manager) user);
		} else if (user instanceof Customer) {
			System.out.println("\nWelcome Customer " + user.getUserFName() + "\n");
			Sale sale = new Sale((Customer) user);
			customerView((Customer) user, sale);
		} else if (user instanceof WarehouseStaff) {
			System.out.println("\nWelcome Warehouse Staff " + user.getUserFName() + "\n");
			warehouseView((WarehouseStaff) user);
			displayMainMenu();
			return;
		}
	}

	private void managerView(Manager manager) {
		Scanner scan = new Scanner(System.in);
		int userInput, value;
		
		do {
			managerMainMenu();
			userInput = scan.nextInt();
			scan.nextLine();
			value = userResponse(userInput);
			
		} while (value != -1);
		
		displayMainMenu();
	}

	// **** ONE QUESTION THOUGH, for the warehouse staff requirement should we
	// complete it by implementing
	// another User class and function to replenish stock levels?
	// then warehouse staff will have to key in supplier details. manager can then
	// view the stuff

	// "I want to be able to replenish stock levels before placing items received on
	// the shelves."

	private void customerView(Customer user, Sale sale) {
		System.out.println("You have " + sale.getItemsInCart() + " items in your cart.");
		if (sale.getItemsInCart() > 0) {
			displayCart(sale);
			System.out.println("Total Price: $" + sale.getTotalPrice());
		}
		System.out.println("-------------------------------------");
		System.out.println("Select one of the following options (1-5):");
		System.out.println("1. Enter Item ID");
		System.out.println("2. Enter Item Name");
		System.out.println("3. Select Item from List");
		System.out.println("4. Finish and Pay");
		System.out.println("5. Cancel Order");
		System.out.println("6. I Need Assistance");
		Scanner sc = new Scanner(System.in);
		int menuIndex = sc.nextInt();
		sc.nextLine();
		if (menuIndex == 1) {
			addProductByID(sale);
			customerView(user, sale);
			return;
		} else if (menuIndex == 2) {
			addProductByName(sale);
			customerView(user, sale);
			return;
		} else if (menuIndex == 3) {
			selectItemFromList(sale);
			customerView(user, sale);
			return;
		} else if (menuIndex == 4) {
			if (!(sale.getItemsInCart() > 0)) {
				System.out.println("No items in cart! Please add item!");
				customerView(user, sale);
				return;
			}
			boolean success = finishAndPay(sale, user);
			if (success == false) {
				customerView(user, sale);
				return;
			} else {
				System.out.println("Thanks for shopping with us! See you soon!");
				displayMainMenu();
				return;
			}
		} else if (menuIndex == 5) {
			System.out.println("Are you sure you would like to cancel the order? (Y/N)");
			String yes = sc.nextLine();
			if (yes.equalsIgnoreCase("n")) {
				customerView(user, sale);
				return;
			} else {
				System.out.println("You've been logged out! See you soon!");
				displayMainMenu();
				return;
			}
		} else if (menuIndex == 6) {
			System.out.println("1. Top Up Credit Card");
			if (sale.getItemsInCart() > 0)
				System.out.println("2. Modify Cart");
			int helpIndex = sc.nextInt();
			sc.nextLine();
			if (helpIndex == 1) {
				topUpCard(user);
				customerView(user, sale);
				return;
			} else if (helpIndex == 2 && sale.getItemsInCart() > 0) {
				modifyCart(sale);
				customerView(user, sale);
				return;
			} else {
				System.out.println("Invalid Input!");
				customerView(user, sale);
				return;
			}
		} else {
			// prints invalid input then rerun?
		}
	}

	private void warehouseView(WarehouseStaff user) {
		Scanner sc = new Scanner(System.in);
		boolean idOK = false;
		boolean nameOK = false;
		boolean inputA = false;
		boolean inputB = false;
		String productName = null;
		String productID = null;
		Product product = null;

		System.out.println("Would you like to add an item received? (Y/N)");
		String yes = sc.nextLine();
		if (yes.equalsIgnoreCase("n")) {
			System.out.println("\nYou've been logged out!");
			return;
		}

		while (idOK == false) {
			System.out.println("Enter product ID:");
			productID = sc.nextLine();
			if (productID == null) {
				System.out.println("Input error! Please try again.");
				continue;
			}
			Product temp = pm.getProduct(productID);
			if (temp == null) {
				System.out.println("Proceeding to add product...");
				idOK = true;
			} else {
				System.out.println("Product with the same ID exists in database!");
			}
		}

		while (nameOK == false) {
			System.out.println("Enter product name:");
			productName = sc.nextLine();
			if (productName == null) {
				System.out.println("Input error! Please try again.");
				continue;
			} else
				nameOK = true;
		}

		System.out.println("Is product weighable? (Y/N)");
		yes = sc.nextLine();
		if (yes.equalsIgnoreCase("n")) {
			double price = 0;
			int quantity = 0;
			while (inputA == false) {
				try {
					System.out.println("Enter unit price ($):");
					price = sc.nextDouble();
					sc.nextLine();
					if (price <= 0) {
						System.out.println("Invalid price! Please try again.");
						continue;
					}
					inputA = true;
				} catch (Exception e) {
					System.out.println("Invalid input! Please try again.");
				}
			}
			while (inputB == false) {
				try {
					System.out.println("Enter stock quantity:");
					quantity = sc.nextInt();
					sc.nextLine();
					if (quantity <= 0) {
						System.out.println("Invalid quantity! Please try again.");
						continue;
					}
					inputB = true;

				} catch (Exception e) {
					System.out.println("Invalid input! Please try again.");
				}
			}
			product = new Product(productID, productName, price, quantity);

			System.out.println("Product ID: " + productID);
			System.out.println("Product Name: " + productName);
			System.out.println("Unit price: " + price);
			System.out.println("Stock quantity: " + quantity);
			System.out.println("Product added!");
		} else {
			double pricePerGram = 0;
			double stockWeight = 0;
			while (inputA == false) {
				try {
					System.out.println("Enter price per 100g:");
					pricePerGram = sc.nextDouble();
					sc.nextLine();
					if (pricePerGram <= 0) {
						System.out.println("Invalid price! Please try again.");
						continue;
					}
					inputA = true;
				} catch (Exception e) {
					System.out.println("Invalid input! Please try again.");
				}
			}
			while (inputB == false) {
				try {
					System.out.println("Enter stock weight in g:");
					stockWeight = sc.nextInt();
					sc.nextLine();
					if (stockWeight <= 0) {
						System.out.println("Invalid weight! Please try again.");
						continue;
					}
					inputB = true;

				} catch (Exception e) {
					System.out.println("Invalid input! Please try again.");
				}
			}
			product = new Product(productID, productName, pricePerGram, stockWeight);

			System.out.println("Product ID: " + productID);
			System.out.println("Product Name: " + productName);
			System.out.println("Price per 100g: " + pricePerGram);
			System.out.println("Stock weight: " + stockWeight);
			System.out.println("Product added!");
		}

		System.out.println("\nNext up, we'll input supplier details for product " + productID + "...");

		boolean supplierCompanyNameOK = false, supplierContactNoOK = false, supplierEmailOK = false,
				supplierLocationOK = false;
		String supplierCompanyName = null, supplierContactNo = null, supplierEmail = null, supplierLocation = null;

		while (supplierCompanyNameOK == false) {
			System.out.println("\nEnter Supplier Company Name:");
			supplierCompanyName = sc.nextLine();
			if (supplierCompanyName == null) {
				System.out.println("Input error! Please try again.");
				continue;
			} else
				supplierCompanyNameOK = true;
		}

		while (supplierContactNoOK == false) {
			System.out.println("\nEnter Supplier Contact Number:");
			supplierContactNo = sc.nextLine();
			if (supplierContactNo == null) {
				System.out.println("Input error! Please try again.");
				continue;
			} else
				supplierContactNoOK = true;
		}

		while (supplierEmailOK == false) {
			System.out.println("\nEnter Supplier Email:");
			supplierEmail = sc.nextLine();
			if (supplierEmail == null) {
				System.out.println("Input error! Please try again.");
				continue;
			} else
				supplierEmailOK = true;
		}

		while (supplierLocationOK == false) {
			System.out.println("\nEnter Supplier Location:");
			supplierLocation = sc.nextLine();
			if (supplierLocation == null) {
				System.out.println("Input error! Please try again.");
				continue;
			} else
				supplierLocationOK = true;
		}

		Supplier supplier = new Supplier(supplierCompanyName, supplierContactNo, supplierEmail, supplierLocation);
		System.out.println("Supplier details added to product " + productID + "!");
		System.out.println("Supplier Company Name:" + supplierCompanyName);
		System.out.println("Supplier Contact No:" + supplierContactNo);
		System.out.println("Supplier Email:" + supplierEmail);
		System.out.println("Supplier Location:" + supplierLocation);
		product.setSupplier(supplier);
		pm.addProduct(product);
		System.out.println("\nThanks for using the system. You've been logged out!");
	}

	private void addProductByID(Sale sale) {
		Scanner sc = new Scanner(System.in);

		boolean quit;
		Product product = null;
		int quantity = 0;
		double weight = 0;
		quit = false;
		while (quit == false) {
			System.out.println("Enter Product ID:");
			String id = sc.nextLine();
			product = pm.getProduct(id);
			if (product == null) {
				System.out.println("Would you like to try again? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				} else {
					continue;
				}
			}
			// check if product is in cart
			ArrayList<SalesLineItem> lineItems = sale.getSalesLineItems();
			SalesLineItem lineItem = null;
			for (int i = 0; i < lineItems.size(); i++) {
				if (lineItems.get(i).getProduct().equals(product)) {
					lineItem = lineItems.get(i);
				}
			}

			if (lineItem == null) {
				if (product.getWeightable() == false) {
					System.out.println("Product isn't weightable!");
					boolean quantityOK = false;
					while (quantityOK == false) {
						System.out.println("Enter Quantity:");
						quantity = sc.nextInt();
						sc.nextLine();
						// verfiication?
						if (quantity > 0 && quantity <= product.getStockQty()) {
							quantityOK = true;
							break;
						} else if (quantity > product.getStockQty()) {
							System.out.println(
									"Quantity inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (quantity <= 0) {
							System.out.println("Quantity error! Would you like to try again? (Y/N)");
						}
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					lineItem = new SalesLineItem(quantity, product);
					sale.addLineItem(lineItem);
					System.out.println(
							"Added " + lineItem.getProductQuantity() + ' ' + product.getProductName() + " to cart!");
					quit = true;
				} else {
					System.out.println("Product weightable!");
					boolean weightOK = false;
					while (weightOK == false) {
						System.out.println(
								"Price per gram for " + product.getProductName() + ":" + product.getPricePerGram());
						System.out.println("Enter Weight (in g):");
						weight = sc.nextDouble();
						sc.nextLine();
						// verfiication?
						if (weight > 0 && weight <= product.getStockWeight()) {
							weightOK = true;
							break;
						} else if (weight > product.getStockWeight()) {
							System.out.println(
									"Weight inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (weight <= 0) {
							System.out.println("Weight error! Would you like to try again? (Y/N)");
						}
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					quit = true;
					lineItem = new SalesLineItem(weight, product, true);
					System.out.println(
							"Added " + lineItem.getWeight() + "g of " + product.getProductName() + " to cart!");
				}

			} else {
				System.out.println("Product already in cart! Would you like to add more? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				}
				if (product.getWeightable() == false) {
					System.out.println("Product isn't weightable!");

					boolean quantityOK = false;
					while (quantityOK == false) {
						System.out.println("Enter Quantity (" + lineItem.getProductQuantity() + " in cart) :");
						quantity = sc.nextInt();
						sc.nextLine();
						// verfiication?
						if (quantity > 0 && quantity <= product.getStockQty()) {
							quantityOK = true;
							break;
						} else if (quantity > product.getStockQty()) {
							System.out.println(
									"Quantity inputted is greater than what we have in stock! Would you like to try again? (Y/N)");
						} else if (quantity <= 0) {
							System.out.println("Quantity error! Would you like to try again? (Y/N)");
						}
						yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					int prodQuantity = lineItem.getProductQuantity();
					prodQuantity += quantity;
					lineItem.setProductQuantity(prodQuantity);
					System.out.println("Added " + quantity + "more " + product.getProductName() + " to cart!");
					System.out.println("Total quantity: " + lineItem.getProductQuantity());
					quit = true;
				} else {
					boolean weightOK = false;
					while (weightOK == false) {
						System.out.println(
								"Price per gram for " + product.getProductName() + ":" + product.getPricePerGram());
						System.out.println("Enter Weight (in g, " + lineItem.getWeight() + "g in cart):");
						weight = sc.nextDouble();
						sc.nextLine();
						// verfiication?
						if (weight > 0 && weight <= product.getStockWeight()) {
							weightOK = true;
							break;
						} else if (weight > product.getStockWeight()) {
							System.out.println(
									"Weight inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (weight <= 0) {
							System.out.println("Weight error! Would you like to try again? (Y/N)");
						}
						yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					double prodWeight = lineItem.getWeight();
					weight += prodWeight;
					lineItem.setWeight(weight);
					System.out.println("Added " + weight + "g of more " + product.getProductName() + " to cart!");
					System.out.println("Total weight: " + lineItem.getWeight() + "g");
					quit = true;
				}
			}
		}

	}

	private void addProductByName(Sale sale) {
		Scanner sc = new Scanner(System.in);

		boolean quit;
		Product product = null;
		int quantity = 0;
		double weight = 0;
		quit = false;
		while (quit == false) {
			System.out.println("Enter Product Name:");
			String name = sc.nextLine();
			product = pm.getProductByName(name);

			if (product == null) {
				System.out.println("Can't find selected product. Would you like to try again? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				} else {
					continue;
				}
			}
			// check if product is in cart
			ArrayList<SalesLineItem> lineItems = sale.getSalesLineItems();
			SalesLineItem lineItem = null;
			for (int i = 0; i < lineItems.size(); i++) {
				if (lineItems.get(i).getProduct().equals(product)) {
					lineItem = lineItems.get(i);
				}
			}

			if (lineItem == null) {
				if (product.getWeightable() == false) {
					System.out.println("Product isn't weightable!");
					boolean quantityOK = false;
					while (quantityOK == false) {
						System.out.println("Enter Quantity:");
						quantity = sc.nextInt();
						sc.nextLine();
						// verfiication?
						if (quantity > 0 && quantity <= product.getStockQty()) {
							quantityOK = true;
							break;
						} else if (quantity > product.getStockQty()) {
							System.out.println(
									"Quantity inputted is greater than what we have in stock! Would you like to try again? (Y/N)");
						} else if (quantity <= 0) {
							System.out.println("Quantity error! Would you like to try again? (Y/N)");
						}
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					lineItem = new SalesLineItem(quantity, product);
					sale.addLineItem(lineItem);
					System.out.println(
							"Added " + lineItem.getProductQuantity() + ' ' + product.getProductName() + " to cart!");
					quit = true;
				} else {
					System.out.println("Product weightable!");
					boolean weightOK = false;
					while (weightOK == false) {
						System.out.println(
								"Price per gram for " + product.getProductName() + ":" + product.getPricePerGram());
						System.out.println("Enter Weight (in g):");
						weight = sc.nextDouble();
						sc.nextLine();
						// verfiication?
						if (weight > 0 && weight <= product.getStockWeight()) {
							weightOK = true;
							break;
						} else if (weight > product.getStockWeight()) {
							System.out.println(
									"Weight inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (weight <= 0) {
							System.out.println("Weight error! Would you like to try again? (Y/N)");
						}
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					quit = true;
					lineItem = new SalesLineItem(weight, product, true);
					System.out.println(
							"Added " + lineItem.getWeight() + "g of " + product.getProductName() + " to cart!");
				}

			} else {
				System.out.println("Product already in cart! Would you like to add more? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				}
				if (product.getWeightable() == false) {
					System.out.println("Product isn't weightable!");
					boolean quantityOK = false;
					while (quantityOK == false) {
						System.out.println("Enter Quantity (" + lineItem.getProductQuantity() + " in cart) :");
						quantity = sc.nextInt();
						sc.nextLine();
						// verfiication?
						if (quantity > 0 && quantity <= product.getStockQty()) {
							quantityOK = true;
							break;
						} else if (quantity > product.getStockQty()) {
							System.out.println(
									"Quantity inputted is greater than what we have in stock! Would you like to try again? (Y/N)");
						} else if (quantity <= 0) {
							System.out.println("Quantity error! Would you like to try again? (Y/N)");
						}
						yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					int prodQuantity = lineItem.getProductQuantity();
					prodQuantity += quantity;
					lineItem.setProductQuantity(prodQuantity);
					System.out.println("Added " + quantity + "more " + product.getProductName() + " to cart!");
					System.out.println("Total quantity: " + lineItem.getProductQuantity());
					quit = true;
				} else {
					boolean weightOK = false;
					while (weightOK == false) {
						System.out.println(
								"Price per gram for " + product.getProductName() + ":" + product.getPricePerGram());
						System.out.println("Enter Weight (in g, " + lineItem.getWeight() + "g in cart):");
						weight = sc.nextDouble();
						sc.nextLine();
						// verfiication?
						if (weight > 0 && weight <= product.getStockWeight()) {
							weightOK = true;
							break;
						} else if (weight > product.getStockWeight()) {
							System.out.println(
									"Weight inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (weight <= 0) {
							System.out.println("Weight error! Would you like to try again? (Y/N)");
						}
						yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					double prodWeight = lineItem.getWeight();
					weight += prodWeight;
					lineItem.setWeight(weight);
					System.out.println("Added " + weight + "g of more " + product.getProductName() + " to cart!");
					System.out.println("Total weight: " + lineItem.getWeight() + "g");
					quit = true;
				}
			}
		}
	}

	private void selectItemFromList(Sale sale) {
		Scanner sc = new Scanner(System.in);
		boolean quit;
		Product product = null;
		int quantity = 0;
		double weight = 0;
		quit = false;
		while (quit == false) {
			System.out.println("Please select one of the following items:");
			HashMap<String, Product> products = pm.getProductsMap();
			int counter = 1;
			System.out.println("#\tID\tName\tWeightable\tPrice");
			for (Product tempProduct : products.values()) {
				if (tempProduct.getWeightable() == true)
					System.out.println(counter + "\t" + tempProduct.getProductId() + '\t' + tempProduct.getProductName()
							+ "\t \t" + tempProduct.getWeightable() + "\t$" + tempProduct.getPricePerGram() + " /g");
				else
					System.out.println(counter + "\t" + tempProduct.getProductId() + '\t' + tempProduct.getProductName()
							+ "\t \t$" + tempProduct.getProductPrice());
				counter++;
			}
			System.out.println("Please enter the product #:");
			int prodNum = sc.nextInt();
			sc.nextLine();

			if (!(prodNum > 0 && prodNum <= counter)) {
				System.out.println("Invalid input! Would you like to try again? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				} else {
					continue;
				}
			}

			counter = 1;

			for (Product tempProduct : products.values()) {
				if (prodNum == counter) {
					product = tempProduct;
				}
				counter++;
			}
			ArrayList<SalesLineItem> lineItems = sale.getSalesLineItems();
			SalesLineItem lineItem = null;
			for (int i = 0; i < lineItems.size(); i++) {
				if (lineItems.get(i).getProduct().equals(product)) {
					lineItem = lineItems.get(i);
				}
			}

			if (lineItem == null) {
				if (product.getWeightable() == false) {
					System.out.println("Product isn't weightable!");
					boolean quantityOK = false;
					while (quantityOK == false) {
						System.out.println("Enter Quantity:");
						quantity = sc.nextInt();
						sc.nextLine();
						// verfiication?
						if (quantity > 0 && quantity <= product.getStockQty()) {
							quantityOK = true;
							break;
						} else if (quantity > product.getStockQty()) {
							System.out.println(
									"Quantity inputted is greater than what we have in stock! Would you like to try again? (Y/N)");
						} else if (quantity <= 0) {
							System.out.println("Quantity error! Would you like to try again? (Y/N)");
						}
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					lineItem = new SalesLineItem(quantity, product);
					sale.addLineItem(lineItem);
					System.out.println(
							"Added " + lineItem.getProductQuantity() + ' ' + product.getProductName() + " to cart!");
					quit = true;
				} else {
					System.out.println("Product weightable!");
					boolean weightOK = false;
					while (weightOK == false) {
						System.out.println(
								"Price per gram for " + product.getProductName() + ":" + product.getPricePerGram());
						System.out.println("Enter Weight (in g):");
						weight = sc.nextDouble();
						sc.nextLine();
						// verfiication?
						if (weight > 0 && weight <= product.getStockWeight()) {
							weightOK = true;
							break;
						} else if (weight > product.getStockWeight()) {
							System.out.println(
									"Weight inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (weight <= 0) {
							System.out.println("Weight error! Would you like to try again? (Y/N)");
						}
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					quit = true;
					lineItem = new SalesLineItem(weight, product, true);
					System.out.println(
							"Added " + lineItem.getWeight() + "g of " + product.getProductName() + " to cart!");
				}

			} else {
				System.out.println("Product already in cart! Would you like to add more? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				}
				if (product.getWeightable() == false) {
					System.out.println("Product isn't weightable!");
					boolean quantityOK = false;
					while (quantityOK == false) {
						System.out.println("Enter Quantity (" + lineItem.getProductQuantity() + " in cart) :");
						quantity = sc.nextInt();
						sc.nextLine();
						// verfiication?
						if (quantity > 0 && quantity <= product.getStockQty()) {
							quantityOK = true;
							break;
						} else if (quantity > product.getStockQty()) {
							System.out.println(
									"Quantity inputted is greater than what we have in stock! Would you like to try again? (Y/N)");
						} else if (quantity <= 0) {
							System.out.println("Quantity error! Would you like to try again? (Y/N)");
						}
						yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					int prodQuantity = lineItem.getProductQuantity();
					prodQuantity += quantity;
					lineItem.setProductQuantity(prodQuantity);
					System.out.println("Added " + quantity + "more " + product.getProductName() + " to cart!");
					System.out.println("Total quantity: " + lineItem.getProductQuantity());
					quit = true;
				} else {
					boolean weightOK = false;
					while (weightOK == false) {
						System.out.println(
								"Price per gram for " + product.getProductName() + ":" + product.getPricePerGram());
						System.out.println("Enter Weight (in g, " + lineItem.getWeight() + "g in cart):");
						weight = sc.nextDouble();
						sc.nextLine();
						// verfiication?
						if (weight > 0 && weight <= product.getStockWeight()) {
							weightOK = true;
							break;
						} else if (weight > product.getStockWeight()) {
							System.out.println(
									"Weight inputted exceeded what we have in stock! Would you like to try again? (Y/N)");
						} else if (weight <= 0) {
							System.out.println("Weight error! Would you like to try again? (Y/N)");
						}
						yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						}
					}
					double prodWeight = lineItem.getWeight();
					weight += prodWeight;
					lineItem.setWeight(weight);
					System.out.println("Added " + weight + "g of more " + product.getProductName() + " to cart!");
					System.out.println("Total weight: " + lineItem.getWeight() + "g");
					quit = true;
				}
			}
		}
	}

	public boolean finishAndPay(Sale sale, Customer customer) {

		Scanner sc = new Scanner(System.in);
		boolean quit = false;
		System.out.println("This is your cart:");
		displayCart(sale);
		System.out.println("Total Price: $" + sale.getTotalPrice());
		System.out.println("Total Loyalty Points Earned: " + sale.getLoyaltyPtsEarned());
		System.out.println("Total Loyalty Points Used: " + sale.getLoyaltyPtsUsed());
		System.out.println("Total Discounted Price: $" + sale.getTotalDiscountedPrice());
		System.out.println("Would you like to finish and pay? (Y/N)");
		String yes = sc.nextLine();
		if (yes.equalsIgnoreCase("n")) {
			return false;
		}
		while (quit == false) {
			System.out.println("Amount Payable: $" + sale.getTotalDiscountedPrice());
			System.out.println("Please enter credit card ID:");
			String credID = sc.nextLine();
			System.out.println("Please enter credit card PIN:");
			String pin = sc.nextLine();
			if (credID.equals(customer.getCreditCard().getCreditCardID()) == false
					|| pin.equals(customer.getCreditCard().getPin()) == false) {
				System.out.println("Credit Card ID / PIN Error! Would you like to try again? (Y/N)");
				yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return false;
				} else
					continue;
			}
			System.out.println("Credit card balance: $" + customer.getCreditCard().getBalance());
			if (customer.getCreditCard().getBalance() < sale.getTotalDiscountedPrice()) {
				System.out.println(
						"Insufficient balance! Would you like one of our friendly staffs to top up for you? (Y/N)");
				yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return false;
				} else {
					boolean topUpSuccessful = topUpCard(customer);
					if (topUpSuccessful == false) {
						System.out.println("Top up failed! Please try again later.");
						return false;
					} else {
						System.out.println("New balance: $" + customer.getCreditCard().getBalance());
					}
				}
			}
			sale.pay(sm);
			customer.getCreditCard().deductBalance(sale.getTotalDiscountedPrice());
			System.out.println("Payment successful! Amount paid: $" + sale.getTotalDiscountedPrice());
			System.out.println("New Credit card balance: $" + customer.getCreditCard().getBalance());
			System.out.println("Loyalty points: " + customer.getLoyaltyPts());
			quit = true;
		}
		return true;
	}

	private boolean topUpCard(Customer customer) {
		boolean quit = false;
		Scanner sc = new Scanner(System.in);
		while (quit == false) {
			System.out.println("Please login to continue or type \"quit\" to quit:");
			System.out.print("\nStaff Username:");
			String userName = sc.nextLine();
			if (userName.equalsIgnoreCase("quit")) {
				return false;
			}
			System.out.print("\nPassword:");
			String pin = sc.nextLine();

			User user = am.verify(userName, pin);
			if (user == null || !(user instanceof SalesStaff)) {
				System.out.println("Login failed! Would you like to try again? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return false;
				} else
					continue;
			} else {
				System.out.println("Enter amount to be topped up:");
				double topupAmt = sc.nextDouble();
				sc.nextLine();
				if (topupAmt <= 0) {
					System.out.println("Input error! You've been logged out, please try again!");
					continue;
				}
				customer.getCreditCard().addBalance(topupAmt);
				System.out.println(
						"Amount topped up: " + topupAmt + " for Customer" + customer.getUserID() + "\'s credit card");
				return true;
			}
		}
		return false;
	}

	private void modifyCart(Sale sale) {
		Scanner sc = new Scanner(System.in);
		boolean quit = false;
		boolean innerQuit = false;
		while (quit == false) {
			System.out.println("Please login to continue or type \"quit\" to quit:");
			System.out.print("\nStaff Username:");
			String userName = sc.nextLine();
			if (userName.equalsIgnoreCase("quit")) {
				return;
			}
			System.out.print("\nPassword:");
			String pin = sc.nextLine();

			User user = am.verify(userName, pin);
			if (user == null || !(user instanceof SalesStaff)) {
				System.out.println("Login failed! Would you like to try again? (Y/N)");
				String yes = sc.nextLine();
				if (yes.equalsIgnoreCase("n")) {
					return;
				} else
					continue;
			} else {
				while (innerQuit == false) {
					displayCart(sale);
					System.out.println("Remove an item by its #:");
					int itemNum = sc.nextInt();
					sc.nextLine();
					if (!(itemNum >= 0 && itemNum <= sale.getItemsInCart())) {
						System.out.println("Invalid input! Would you like to try again? (Y/N)");
						String yes = sc.nextLine();
						if (yes.equalsIgnoreCase("n")) {
							return;
						} else {
							continue;
						}
					} else {
						sale.getSalesLineItems().remove(itemNum);
						System.out.println("Item removed!!");
					}
					innerQuit = true;
				}
			}
			quit = true;
		}
	}

	private void displayCart(Sale sale) {
		ArrayList<SalesLineItem> lineItems = sale.getSalesLineItems();
		System.out.println("#\tID\tName\tQuantity\tWeight\tPrice");
		for (int i = 0; i < sale.getItemsInCart(); i++) {
			Product tempProduct = lineItems.get(i).getProduct();
			SalesLineItem lineItem = lineItems.get(i);
			if (lineItem.getWeightable() == true)
				System.out.println(i + "\t" + tempProduct.getProductId() + '\t' + tempProduct.getProductName() + "\t \t"
						+ lineItem.getWeight() + "g\t$" + lineItem.getTotalPrice());
			else
				System.out.println(i + "\t" + tempProduct.getProductId() + '\t' + tempProduct.getProductName() + '\t'
						+ lineItem.getProductQuantity() + "\t \t$" + lineItem.getTotalPrice());
		}
	}

	
	
	// Manager Functions
	public void managerMainMenu() {
		System.out.println("Select one of the following options (1-7):");
		System.out.println("1. Override standard price for a specific product");
		System.out.println("2. Apply discount on item");
		System.out.println("3. Check stock levels");
		System.out.println("4. Generate sales report");
		System.out.println("5. List products generating the most revenue");
		System.out.println("6. Display existing products");
		System.out.println("7. Logout");
		System.out.print("\nPlease enter your choice: ");
	}
	
	public int userResponse(int userResp) {
		switch (userResp) {
			case 1:
				overridePrice();
				break;
			case 2: 
				applyDiscount();
				break;
			case 3:
				displayStockLevels();
				break;
			case 4:
				displaySalesReport();
				break;
			case 5:
				displayTopRevenueProducts();
				break;
			case 6:
				displayExistingProducts();
				break;
			case 7:
				return -1;
			default:
				System.out.println("Invalid response");
		}
		return 0;
	}
	
	public void overridePrice() {
		Scanner scan = new Scanner(System.in);
		double newPrice = 0;
		String productID = null;
		
		System.out.print("Please enter the product ID: ");
		productID = scan.nextLine();
		Product temp = pm.getProduct(productID);

		if (temp == null) {
			System.out.println();
			return;
		} else {
			System.out.println("Current Price: $" + temp.getProductPrice());
			
			System.out.print("Please enter the new price for the product: ");
			newPrice = scan.nextDouble();
			scan.nextLine();

//				product = new Product(productID, newPrice);

			temp.setProductPrice(newPrice);

			System.out.println("The new price for product ID: " + temp.getProductId() + " is now $" + temp.getProductPrice());
			System.out.println("\nWould you like to override a different product price? (Y/N)");
			String userChoice = scan.nextLine();
			
			if (userChoice.equalsIgnoreCase("n")) {
				System.out.println();
				return;
			} else {
				overridePrice();
			}
		}
	}
	
	public void applyDiscount() {
		Scanner scan = new Scanner(System.in);
		String productID = null;
		
		System.out.print("Please enter the product ID: ");
		productID = scan.nextLine();
		Product temp = pm.getProduct(productID);

		if (temp == null) {
			System.out.println();
			return;
		} else {
			System.out.print("How much discount would you like to apply on this item? ");
			double discountPercentage = scan.nextDouble();

			temp.setDiscountedPrice(discountPercentage);

			System.out.println("Product ID: " + temp.getProductId() + " Old Price: $" + temp.getProductPrice() + "\n");
			System.out.println(temp.getDiscountRate() + "% " + "discount has been applied to " + temp.getProductName());
			System.out.println("Product ID: " + temp.getProductId() + " is now $" + temp.getDiscountedPrice() + "\n");

		}
	}
	
	public void displayStockLevels() {
		Scanner scan = new Scanner(System.in);
		String productID = null;
		
		System.out.print("Please enter the ID of the product you would like to restock: ");
		productID = scan.nextLine();

		Product temp = pm.getProduct(productID);

		if (temp == null) {
			System.out.println("Product does not exist.");
			displayStockLevels();
		} else {

			System.out.println("The Stock Quantity for this product: " + temp.getStockQty());
			System.out.println("Would you like to do a restock? (Y/N)");
			String yes = scan.nextLine();

			if (yes.equalsIgnoreCase("n")) {
				System.out.println();
				return;
			}

			System.out.print("Enter restock quantity: ");
			int restockQty = scan.nextInt(); // please do input verification
			scan.nextLine();
			temp.addStockQty(restockQty);
			System.out.println("Successfully restocked items");
			System.out.println("New stock quantity: " + temp.getStockQty() + "\n");

			if (temp.getRestockLvl() == 0 || temp.getBulkSalesQty() == 0) {
				System.out.println("Auto restock is not enabled for this product!");
				System.out.println("Would you like to setup auto restock for this product? (Y/N)");
			} else {
				System.out.println("Current auto restock level: " + temp.getRestockLvl());
				System.out.println("Current auto restock quantity: " + temp.getReorderQty());
				System.out.println("Would you like to modify auto restock for this product? (Y/N)");
			}

			yes = scan.nextLine();

			if (yes.equalsIgnoreCase("n")) {
				System.out.println();
				return;
			}

			System.out.print("Enter auto restock level: ");
			int autoRestockLevel = scan.nextInt();
			scan.nextLine();
			temp.setRestockLvl(autoRestockLevel);

			System.out.print("Enter auto restock quantity: ");
			int autoRestockQty = scan.nextInt();
			scan.nextLine();
			temp.setReorderQty(autoRestockQty);

			System.out.println("Current auto restock level: " + temp.getRestockLvl());
			System.out.println("Current auto restock quantity: " + temp.getReorderQty() + "\n");

		}
	}
	
	public void displaySalesReport() {
		HashMap<String, Product> products = pm.getProductsMap();
		Iterator iterator = products.entrySet().iterator();
		while (iterator.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) iterator.next();
			Product temp = pm.getProduct(pair.getKey().toString());
			System.out.println("Product Name: " + temp.getProductName());
			System.out.println("Amount Sold: " + temp.getAmountSold());
			System.out.println("Product Reveneue: $" + temp.getRevenueGenerated());
			System.out.println();
		}
	}
	
	public void displayTopRevenueProducts() {
		HashMap<String, Product> products = pm.getProductsMap();

		ArrayList<String> mapKeys = new ArrayList<>(products.keySet());
		ArrayList<Double> mapValues = new ArrayList<Double>();

		Iterator iterator = products.entrySet().iterator();
		while (iterator.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) iterator.next();
			mapValues.add(pm.getProduct(pair.getKey().toString()).getRevenueGenerated());
		}
		
		double highestRevenue = 0.01, secondHighestRevenue = 0.01, thirdHighestRevenue = 0.01;
		String idHighestRevenue = null, idSecondHighestRevenue = null, idThirdHighestRevenue = null;
		for (int i = 0; i < mapValues.size(); i++) {
			if (mapValues.get(i) > highestRevenue) {
				thirdHighestRevenue = secondHighestRevenue;
				idThirdHighestRevenue = idSecondHighestRevenue;
				secondHighestRevenue = highestRevenue;
				idSecondHighestRevenue = idHighestRevenue;
				highestRevenue = mapValues.get(i);
				idHighestRevenue = mapKeys.get(i);
			} else if (mapValues.get(i) > secondHighestRevenue) {
				thirdHighestRevenue = secondHighestRevenue;
				idThirdHighestRevenue = idSecondHighestRevenue;
				secondHighestRevenue = mapValues.get(i);
				idSecondHighestRevenue = mapKeys.get(i);
			} else if (mapValues.get(i) > thirdHighestRevenue) {
				thirdHighestRevenue = mapValues.get(i);
				idThirdHighestRevenue = mapKeys.get(i);
			}
		}

		if (idHighestRevenue == null) {
			System.out.println("No Items Have Been Sold");
		}
		if (idHighestRevenue != null) {
			System.out.println("Top Three Higest Revenue Items");
			System.out.println(
					"1. " + pm.getProduct(idHighestRevenue).getProductName() + " | $" + highestRevenue);
		}
		if (idSecondHighestRevenue != null) {
			System.out.println("2. " + pm.getProduct(idSecondHighestRevenue).getProductName() + " | $"
					+ secondHighestRevenue);
		}
		if (idThirdHighestRevenue != null) {
			System.out.println("3. " + pm.getProduct(idThirdHighestRevenue).getProductName() + " | $"
					+ thirdHighestRevenue);
		}
		System.out.println();
	}
	
	public void displayExistingProducts() {
		pm.printItems();
	}
	
	
	
	
	
	
	
}