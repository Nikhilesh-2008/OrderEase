library(DBI)
library(RMariaDB)

connect_database <- function() {
  
  password <- Sys.getenv("ORDEREASE_DB_PASSWORD")
  
  print(paste("Password loaded:", nchar(password) > 0))
  
  con <- dbConnect(
    RMariaDB::MariaDB(),
    host = "localhost",
    port = 3306,
    dbname = "orderease",
    username = "root",
    password = password
  )
  
  return(con)
}