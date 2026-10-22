library(DBI)
library(RMariaDB)

connect_database <- function() {
  
  password <- Sys.getenv("ORDEREASE_DB_PASSWORD")
  if (is.null(password) || is.na(password) || nchar(trimws(password)) == 0) {
    password <- "Nikhilesh@2008"
  }
  
  con <- tryCatch({
    dbConnect(
      RMariaDB::MariaDB(),
      host = "127.0.0.1",
      port = 3306,
      dbname = "orderease",
      username = "root",
      password = password
    )
  }, error = function(e) {
    dbConnect(
      RMariaDB::MariaDB(),
      host = "localhost",
      port = 3306,
      dbname = "orderease",
      username = "root",
      password = password
    )
  })
  
  return(con)
}
