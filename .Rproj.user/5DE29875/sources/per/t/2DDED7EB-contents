# ============================================================
# ORDEREASE - SMART CANTEEN ANALYTICS DASHBOARD
# Language: R
# Framework: Shiny
# Database: MySQL
# ============================================================

library(shiny)
library(ggplot2)
library(DBI)
library(RMariaDB)
library(dplyr)
library(DT)
library(lubridate)
library(scales)
library(bslib)

source("db_connection.R")


# ============================================================
# THEME AND COLORS
# ============================================================

navy <- "#121826"
card_color <- "#1E2738"
orange <- "#FF914D"
blue <- "#4DA3FF"
green <- "#22C55E"
red <- "#F87171"
text_color <- "#F3F4F6"

dashboard_theme <- bs_theme(
  version = 5,
  bg = navy,
  fg = text_color,
  primary = orange,
  secondary = card_color,
  success = green,
  base_font = font_google("Roboto")
)


# ============================================================
# LOAD DATA FROM MYSQL
# ============================================================

get_order_data <- function() {
  
  con <- connect_database()
  
  on.exit(dbDisconnect(con), add = TRUE)
  
  sql <- "
    SELECT
      o.order_id,
      o.order_time,
      o.total_amount,
      o.status,
      oi.item_id,
      m.item_name,
      oi.quantity,
      oi.unit_price
    FROM orders o
    LEFT JOIN order_items oi
      ON o.order_id = oi.order_id
    LEFT JOIN menu m
      ON oi.item_id = m.item_id
    ORDER BY o.order_time
  "
  
  data <- dbGetQuery(con, sql)
  
  if (nrow(data) > 0) {
    
    data$order_time <- as.POSIXct(data$order_time)
    data$status <- as.character(data$status)
    
    data$item_name[is.na(data$item_name)] <- "Unknown Item"
    
  }
  
  data
}


# ============================================================
# HELPER: GET ONE ROW PER ORDER
# ============================================================

get_unique_orders <- function(data) {
  
  if (nrow(data) == 0) {
    return(data)
  }
  
  data %>%
    arrange(order_time) %>%
    distinct(order_id, .keep_all = TRUE)
  
}


# ============================================================
# HELPER: COMMON CHART THEME
# ============================================================

chart_theme <- function() {
  
  theme_minimal(base_size = 12) +
    
    theme(
      plot.background = element_rect(
        fill = card_color,
        color = NA
      ),
      
      panel.background = element_rect(
        fill = card_color,
        color = NA
      ),
      
      panel.grid.major = element_line(
        color = "#354052"
      ),
      
      panel.grid.minor = element_blank(),
      
      text = element_text(
        color = text_color
      ),
      
      plot.title = element_text(
        color = orange,
        face = "bold",
        size = 14
      ),
      
      axis.title = element_text(
        color = text_color
      ),
      
      axis.text = element_text(
        color = text_color
      ),
      
      legend.background = element_rect(
        fill = card_color,
        color = NA
      ),
      
      legend.text = element_text(
        color = text_color
      )
    )
  
}


# ============================================================
# USER INTERFACE
# ============================================================

ui <- fluidPage(
  
  theme = dashboard_theme,
  
  tags$head(
    
    tags$style(HTML("
      body {
        background-color: #121826;
      }

      .container-fluid {
        padding: 20px;
      }

      .well {
        background-color: #1E2738;
        border: 1px solid #354052;
        border-radius: 12px;
        color: #F3F4F6;
      }

      .card-box {
        background-color: #1E2738;
        border: 1px solid #354052;
        border-radius: 12px;
        padding: 18px;
        margin-bottom: 18px;
        min-height: 115px;
      }

      .metric-label {
        color: #B0BEC5;
        font-size: 15px;
        margin-bottom: 10px;
      }

      .metric-value {
        color: #FF914D;
        font-size: 27px;
        font-weight: bold;
      }

      .section-title {
        color: #FF914D;
        font-weight: bold;
        margin-bottom: 15px;
      }

      .chart-box {
        background-color: #1E2738;
        border: 1px solid #354052;
        border-radius: 12px;
        padding: 15px;
        margin-bottom: 20px;
      }

      .form-control {
        border-radius: 6px;
      }

      .btn {
        border-radius: 7px;
      }

      table.dataTable {
        color: #F3F4F6 !important;
      }

      .dataTables_wrapper {
        color: #F3F4F6 !important;
      }

      .dataTables_filter input,
      .dataTables_length select {
        color: #F3F4F6 !important;
        background-color: #1E2738 !important;
      }

      .footer-text {
        color: #B0BEC5;
        text-align: center;
        padding: 20px;
      }
    "))
    
  ),
  
  
  # ---------------- PAGE HEADER ----------------
  
  fluidRow(
    
    column(
      12,
      
      h1(
        "OrderEase Analytics",
        style = paste0(
          "color:", orange,
          ";font-weight:bold;margin-bottom:5px;"
        )
      ),
      
      p(
        "Smart Canteen Management and Sales Dashboard",
        style = paste0(
          "color:", text_color,
          ";font-size:16px;margin-bottom:25px;"
        )
      )
      
    )
    
  ),
  
  
  sidebarLayout(
    
    sidebarPanel(
      
      h4(
        "Dashboard Filters",
        class = "section-title"
      ),
      
      dateRangeInput(
        "date_range",
        "Select Date Range",
        start = Sys.Date() - 30,
        end = Sys.Date(),
        format = "yyyy-mm-dd"
      ),
      
      selectInput(
        "status",
        "Order Status",
        choices = c(
          "All",
          "PLACED",
          "PREPARING",
          "READY",
          "COLLECTED"
        ),
        selected = "All"
      ),
      
      actionButton(
        "refresh",
        "Refresh Dashboard",
        icon = icon("rotate"),
        class = "btn-primary",
        width = "100%"
      ),
      
      br(),
      br(),
      
      tags$hr(),
      
      p(
        "Data Source",
        style = "font-weight:bold;color:#FF914D;"
      ),
      
      p("OrderEase MySQL Database"),
      
      p(
        "Visualizations created using R and ggplot2."
      )
      
    ),
    
    
    mainPanel(
      
      # ---------------- KPI CARDS ----------------
      
      h3(
        "Business Overview",
        class = "section-title"
      ),
      
      fluidRow(
        
        column(
          4,
          
          div(
            class = "card-box",
            
            div(
              class = "metric-label",
              "Total Orders"
            ),
            
            div(
              class = "metric-value",
              textOutput("total_orders")
            )
            
          )
          
        ),
        
        column(
          4,
          
          div(
            class = "card-box",
            
            div(
              class = "metric-label",
              "Total Revenue"
            ),
            
            div(
              class = "metric-value",
              textOutput("total_revenue")
            )
            
          )
          
        ),
        
        column(
          4,
          
          div(
            class = "card-box",
            
            div(
              class = "metric-label",
              "Average Order Value"
            ),
            
            div(
              class = "metric-value",
              textOutput("average_order")
            )
            
          )
          
        )
        
      ),
      
      
      # ---------------- FIRST CHART ROW ----------------
      
      fluidRow(
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Daily Revenue Trend",
              class = "section-title"
            ),
            
            plotOutput(
              "daily_sales",
              height = "300px"
            )
            
          )
          
        ),
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Popular Food Items",
              class = "section-title"
            ),
            
            plotOutput(
              "popular_items",
              height = "300px"
            )
            
          )
          
        )
        
      ),
      
      
      # ---------------- SECOND CHART ROW ----------------
      
      fluidRow(
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Order Status Distribution",
              class = "section-title"
            ),
            
            plotOutput(
              "order_status",
              height = "300px"
            )
            
          )
          
        ),
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Revenue Distribution",
              class = "section-title"
            ),
            
            plotOutput(
              "revenue_histogram",
              height = "300px"
            )
            
          )
          
        )
        
      ),
      
      
      # ---------------- THIRD CHART ROW ----------------
      
      fluidRow(
        
        column(
          12,
          
          div(
            class = "chart-box",
            
            h4(
              "Quantity vs Item Revenue",
              class = "section-title"
            ),
            
            plotOutput(
              "quantity_scatter",
              height = "320px"
            )
            
          )
          
        )
        
      ),
      
      
      # ---------------- ORDER DETAILS ----------------
      
      fluidRow(
        
        column(
          12,
          
          div(
            class = "chart-box",
            
            h4(
              "Order Details",
              class = "section-title"
            ),
            
            DTOutput("orders_table")
            
          )
          
        )
        
      )
      
    )
    
  ),
  
  
  # ---------------- FOOTER ----------------
  
  tags$div(
    class = "footer-text",
    "OrderEase | R Analytics Dashboard | Academic Project"
  )
  
)


# ============================================================
# SERVER LOGIC
# ============================================================

server <- function(input, output, session) {
  
  
  # ---------------- LOAD DATA ----------------
  
  orders_data <- eventReactive(
    
    input$refresh,
    
    {
      
      get_order_data()
      
    },
    
    ignoreNULL = FALSE
    
  )
  
  
  # ---------------- FILTER DATA ----------------
  
  filtered_data <- reactive({
    
    data <- orders_data()
    
    if (nrow(data) == 0) {
      return(data)
    }
    
    if (
      is.null(input$date_range) ||
      length(input$date_range) != 2 ||
      any(is.na(input$date_range))
    ) {
      return(data[0, ])
    }
    
    start_date <- as.Date(input$date_range[1])
    end_date <- as.Date(input$date_range[2])
    
    data <- data %>%
      
      filter(
        as.Date(order_time) >= start_date,
        as.Date(order_time) <= end_date
      )
    
    if (
      !is.null(input$status) &&
      input$status != "All"
    ) {
      
      data <- data %>%
        filter(status == input$status)
      
    }
    
    data
    
  })
  
  
  # ==========================================================
  # KPI 1: TOTAL ORDERS
  # ==========================================================
  
  output$total_orders <- renderText({
    
    data <- filtered_data()
    
    length(unique(data$order_id))
    
  })
  
  
  # ==========================================================
  # KPI 2: TOTAL REVENUE
  # ==========================================================
  
  output$total_revenue <- renderText({
    
    data <- get_unique_orders(filtered_data())
    
    amount <- sum(
      data$total_amount,
      na.rm = TRUE
    )
    
    paste0(
      "\u20B9 ",
      format(
        round(amount, 2),
        big.mark = ",",
        nsmall = 2
      )
    )
    
  })
  
  
  # ==========================================================
  # KPI 3: AVERAGE ORDER VALUE
  # ==========================================================
  
  output$average_order <- renderText({
    
    data <- get_unique_orders(filtered_data())
    
    amount <- if (nrow(data) == 0) {
      
      0
      
    } else {
      
      mean(
        data$total_amount,
        na.rm = TRUE
      )
      
    }
    
    paste0(
      "\u20B9 ",
      format(
        round(amount, 2),
        big.mark = ",",
        nsmall = 2
      )
    )
    
  })
  
  
  # ==========================================================
  # CHART 1: DAILY REVENUE LINE CHART
  # ==========================================================
  
  output$daily_sales <- renderPlot({
    
    data <- get_unique_orders(filtered_data())
    
    validate(
      need(
        nrow(data) > 0,
        "No orders found for the selected filters."
      )
    )
    
    daily_data <- data %>%
      
      mutate(
        date = as.Date(order_time)
      ) %>%
      
      group_by(date) %>%
      
      summarise(
        revenue = sum(total_amount, na.rm = TRUE),
        .groups = "drop"
      )
    
    ggplot(
      daily_data,
      aes(
        x = date,
        y = revenue
      )
    ) +
      
      geom_line(
        color = blue,
        linewidth = 1.2
      ) +
      
      geom_point(
        color = orange,
        size = 3
      ) +
      
      scale_y_continuous(
        labels = label_number(prefix = "\u20B9 ")
      ) +
      
      labs(
        x = "Date",
        y = "Revenue"
      ) +
      
      chart_theme()
    
  })
  
  
  # ==========================================================
  # CHART 2: POPULAR FOOD ITEMS
  # ==========================================================
  
  output$popular_items <- renderPlot({
    
    data <- filtered_data()
    
    item_data <- data %>%
      
      filter(!is.na(item_id)) %>%
      
      group_by(item_name) %>%
      
      summarise(
        quantity_sold = sum(quantity, na.rm = TRUE),
        .groups = "drop"
      ) %>%
      
      arrange(quantity_sold)
    
    validate(
      need(
        nrow(item_data) > 0,
        "No food item data available."
      )
    )
    
    ggplot(
      item_data,
      aes(
        x = reorder(item_name, quantity_sold),
        y = quantity_sold
      )
    ) +
      
      geom_col(
        fill = orange,
        width = 0.7
      ) +
      
      coord_flip() +
      
      labs(
        x = "Food Item",
        y = "Quantity Sold"
      ) +
      
      chart_theme()
    
  })
  
  
  # ==========================================================
  # CHART 3: ORDER STATUS PIE CHART WITH PERCENTAGES
  # ==========================================================
  
  output$order_status <- renderPlot({
    
    data <- get_unique_orders(filtered_data())
    
    validate(
      need(
        nrow(data) > 0,
        "No order data available."
      )
    )
    
    status_data <- data %>%
      
      count(status, name = "total") %>%
      
      mutate(
        
        percentage = total / sum(total) * 100,
        
        label = paste0(
          status,
          "\n",
          round(percentage, 1),
          "%"
        )
        
      )
    
    ggplot(
      status_data,
      aes(
        x = "",
        y = total,
        fill = status
      )
    ) +
      
      geom_col(
        width = 1,
        color = navy
      ) +
      
      coord_polar(theta = "y") +
      
      geom_text(
        aes(label = label),
        position = position_stack(vjust = 0.5),
        color = "white",
        size = 3.5
      ) +
      
      scale_fill_brewer(
        palette = "Set2"
      ) +
      
      labs(
        fill = "Status"
      ) +
      
      theme_void() +
      
      theme(
        plot.background = element_rect(
          fill = card_color,
          color = NA
        ),
        
        legend.background = element_rect(
          fill = card_color,
          color = NA
        ),
        
        legend.text = element_text(
          color = text_color
        ),
        
        legend.title = element_text(
          color = orange
        )
      )
    
  })
  
  
  # ==========================================================
  # CHART 4: REVENUE HISTOGRAM
  # ==========================================================
  
  output$revenue_histogram <- renderPlot({
    
    data <- get_unique_orders(filtered_data())
    
    validate(
      need(
        nrow(data) > 0,
        "No order data available."
      )
    )
    
    ggplot(
      data,
      aes(x = total_amount)
    ) +
      
      geom_histogram(
        bins = 10,
        fill = orange,
        color = navy,
        linewidth = 0.5
      ) +
      
      scale_x_continuous(
        labels = label_number(prefix = "\u20B9 ")
      ) +
      
      labs(
        x = "Order Value",
        y = "Number of Orders"
      ) +
      
      chart_theme()
    
  })
  
  
  # ==========================================================
  # CHART 5: QUANTITY VS ITEM REVENUE SCATTER PLOT
  # ==========================================================
  
  output$quantity_scatter <- renderPlot({
    
    data <- filtered_data() %>%
      
      filter(
        !is.na(quantity),
        !is.na(unit_price)
      ) %>%
      
      mutate(
        item_revenue = quantity * unit_price
      )
    
    validate(
      need(
        nrow(data) > 0,
        "No order item data available."
      )
    )
    
    ggplot(
      data,
      aes(
        x = quantity,
        y = item_revenue
      )
    ) +
      
      geom_point(
        color = blue,
        size = 3,
        alpha = 0.8
      ) +
      
      labs(
        x = "Quantity",
        y = "Item Revenue (Rs.)"
      ) +
      
      chart_theme()
    
  })
  
  
  # ==========================================================
  # ORDER DETAILS TABLE
  # ==========================================================
  
  output$orders_table <- renderDT({
    
    data <- filtered_data()
    
    if (nrow(data) == 0) {
      
      return(
        
        datatable(
          data.frame(
            Message = "No orders available for the selected filters."
          ),
          
          options = list(
            dom = "t"
          ),
          
          rownames = FALSE
          
        )
        
      )
      
    }
    
    display_data <- data %>%
      
      select(
        order_id,
        order_time,
        item_name,
        quantity,
        unit_price,
        total_amount,
        status
      ) %>%
      
      mutate(
        
        order_time = format(
          order_time,
          "%d-%b-%Y %H:%M"
        )
        
      )
    
    names(display_data) <- c(
      "Order ID",
      "Order Time",
      "Food Item",
      "Quantity",
      "Unit Price (Rs.)",
      "Order Total (Rs.)",
      "Status"
    )
    
    datatable(
      
      display_data,
      
      rownames = FALSE,
      
      filter = "top",
      
      options = list(
        pageLength = 5,
        scrollX = TRUE,
        autoWidth = TRUE
      )
      
    )
    
  })
  
}


# ============================================================
# RUN SHINY APPLICATION
# ============================================================

shinyApp(
  ui = ui,
  server = server
)