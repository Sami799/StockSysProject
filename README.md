# StockSys

StockSys is a Java-based stock and delivery management system with a graphical user interface (GUI).  
It was developed to support managers in handling regions, members, and deliveries efficiently, with persistent storage handled via JSON and file I/O.

## 🚀 Features
- **Login System**: Secure login for managers with credentials stored in files.  
- **Region Management**: Each manager controls a region (North, South, Center) and can add/remove members.  
- **Delivery Management**: Add and manage different types of deliveries (short & business deliveries).  
- **File I/O**: Reads/writes data from JSON files, including users, deliveries, members, and history.  
- **User-Friendly GUI**: Designed with JavaFX, styled via CSS and images.  

## 🛠️ Tech Stack
- **Java 17+** (Maven project)  
- **JavaFX** for GUI  
- **JSON** for data storage  
- **Maven** for build & dependency management  

## 📂 Project Structure
- `src/` → Application source code (controllers, utils, models)  
- `resources/` → CSS, images, and JSON files for storage  
- `pom.xml` → Maven configuration  
- `StockSys_User_Manual.pdf` → Detailed usage manual  
