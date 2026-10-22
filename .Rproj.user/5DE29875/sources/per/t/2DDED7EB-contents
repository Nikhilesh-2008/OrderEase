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
      COALESCE(m.item_name, 'Unknown Item') AS item_name,
      COALESCE(m.canteen, 'Snacks & Shakes Corner') AS canteen,
      COALESCE(m.category, 'Fast Food') AS category,
      COALESCE(oi.quantity, 1) AS quantity,
      COALESCE(oi.unit_price, o.total_amount) AS unit_price
    FROM orders o
    LEFT JOIN order_items oi
      ON o.order_id = oi.order_id
    LEFT JOIN menu m
      ON oi.item_id = m.item_id
    ORDER BY o.order_time ASC
  "
  
  data <- dbGetQuery(con, sql)
  
  if (nrow(data) > 0) {
    
    data$order_time <- as.POSIXct(data$order_time)
    data$status <- as.character(data$status)
    data$item_name[is.na(data$item_name)] <- "Unknown Item"
    data$canteen <- as.character(data$canteen)
    data$category <- as.character(data$category)
    data$quantity <- as.numeric(data$quantity)
    data$unit_price <- as.numeric(data$unit_price)
    data$total_amount <- as.numeric(data$total_amount)
    
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

      .chart-desc {
        color: #B0BEC5;
        font-size: 13px;
        line-height: 1.5;
        margin-top: 10px;
        padding: 8px 12px;
        background-color: rgba(18, 24, 38, 0.7);
        border-left: 3px solid #FF914D;
        border-radius: 4px;
      }

      .chart-desc strong {
        color: #FF914D;
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
        start = Sys.Date() - 60,
        end = Sys.Date() + 1,
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
      
      p("OrderEase MySQL Database (orderease)"),
      
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
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Plots daily gross revenue across calendar dates. It highlights revenue consistency and helps managers spot revenue surges during peak academic days or college events."
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
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Ranks the most frequently ordered items across campus canteens. Kitchen managers use this data to prioritize ingredient preparation and prevent stockouts for high-demand meals."
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
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Visualizes the lifecycle distribution of orders (Placed, Preparing, Ready, Collected). Provides real-time visibility into kitchen turnaround speed and active queue clearance."
            )
            
          )
          
        ),
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Revenue Share by Food Category",
              class = "section-title"
            ),
            
            plotOutput(
              "category_donut",
              height = "300px"
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Shows the percentage of campus revenue generated across categories (Fast Food, Meals, Snacks, Beverages). Reveals which culinary segments drive the highest financial yield."
            )
            
          )
          
        )
        
      ),
      
      
      # ---------------- THIRD CHART ROW ----------------
      
      fluidRow(
        
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
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Displays the frequency distribution of total order ticket amounts. Highlights typical student spending ranges (₹40 to ₹150) to assist in pricing meal combos."
            )
            
          )
          
        ),
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Campus Outlet Sales Comparison",
              class = "section-title"
            ),
            
            plotOutput(
              "outlet_sales",
              height = "300px"
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Compares total generated revenue across campus outlets (Food Court, Snacks Corner, Chai & Juice Bar). Shows where student footfall generates the highest revenue to guide counter staffing."
            )
            
          )
          
        )
        
      ),
      
      
      # ---------------- FOURTH CHART ROW ----------------
      
      fluidRow(
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Quantity vs Item Revenue",
              class = "section-title"
            ),
            
            plotOutput(
              "quantity_scatter",
              height = "300px"
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Analyzes the relationship between items purchased in an order line and total revenue earned. Points situated higher up identify premium menu items compared to bulk affordable snacks."
            )
            
          )
          
        ),
        
        column(
          6,
          
          div(
            class = "chart-box",
            
            h4(
              "Peak Ordering Hours & Traffic",
              class = "section-title"
            ),
            
            plotOutput(
              "hourly_traffic",
              height = "300px"
            ),
            
            div(
              class = "chart-desc",
              tags$strong("What this graph explains: "),
              "Plots order volumes by hour of the day (24-hour campus time). Pinpoints lunch and evening tea rush hours to help kitchen staff manage preparation and break-time surges."
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
  
  
  # ---------------- AUTO-ALIGN DATE RANGE ----------------
  
  observe({
    data <- orders_data()
    if (nrow(data) > 0) {
      min_date <- as.Date(min(data$order_time, na.rm = TRUE))
      max_date <- as.Date(max(data$order_time, na.rm = TRUE)) + 1
      updateDateRangeInput(
        session,
        "date_range",
        start = min_date,
        end = max_date
      )
    }
  })
  
  
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
      return(data)
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
        
        panel.background = element_rect(
          fill = card_color,
          color = NA
        ),
        
        legend.background = element_rect(
          fill = card_color,
          color = NA
        ),
        
        legend.key = element_rect(
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
    
  }, bg = card_color)
  
  
  # ==========================================================
  # CHART 4: REVENUE SHARE BY FOOD CATEGORY (DOUGHNUT CHART)
  # ==========================================================
  
  output$category_donut <- renderPlot({
    
    data <- filtered_data()
    
    validate(
      need(
        nrow(data) > 0,
        "No category sales available."
      )
    )
    
    cat_data <- data %>%
      filter(!is.na(category), !is.na(quantity), !is.na(unit_price)) %>%
      mutate(item_revenue = quantity * unit_price) %>%
      group_by(category) %>%
      summarise(
        revenue = sum(item_revenue, na.rm = TRUE),
        .groups = "drop"
      ) %>%
      mutate(
        percentage = revenue / sum(revenue) * 100,
        label = paste0(category, "\n", round(percentage, 1), "%")
      ) %>%
      arrange(desc(revenue))
    
    validate(
      need(
        nrow(cat_data) > 0,
        "No category revenue data available."
      )
    )
    
    ggplot(cat_data, aes(x = 2, y = revenue, fill = category)) +
      geom_bar(stat = "identity", width = 1, color = card_color) +
      coord_polar(theta = "y") +
      xlim(0.5, 2.5) +
      geom_text(
        aes(label = label),
        position = position_stack(vjust = 0.5),
        color = "white",
        size = 3.5,
        fontface = "bold"
      ) +
      scale_fill_brewer(palette = "Set3") +
      theme_void() +
      theme(
        plot.background = element_rect(fill = card_color, color = NA),
        panel.background = element_rect(fill = card_color, color = NA),
        legend.background = element_rect(fill = card_color, color = NA),
        legend.key = element_rect(fill = card_color, color = NA),
        legend.text = element_text(color = text_color),
        legend.title = element_text(color = orange, face = "bold")
      ) +
      labs(fill = "Category")
    
  }, bg = card_color)
  
  
  # ==========================================================
  # CHART 5: REVENUE HISTOGRAM
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
      
      scale_y_continuous(
        breaks = function(x) {
          upper <- max(1, ceiling(max(x, na.rm = TRUE)))
          step <- max(1, ceiling(upper / 5))
          seq(0, upper, by = step)
        },
        labels = label_number(accuracy = 1),
        expand = expansion(mult = c(0, 0.05))
      ) +
      
      labs(
        x = "Order Value",
        y = "Number of Orders"
      ) +
      
      chart_theme()
    
  })
  
  
  # ==========================================================
  # CHART 6: CAMPUS OUTLET SALES COMPARISON (BAR CHART)
  # ==========================================================
  
  output$outlet_sales <- renderPlot({
    
    data <- filtered_data()
    
    validate(
      need(
        nrow(data) > 0,
        "No canteen outlet data available."
      )
    )
    
    outlet_data <- data %>%
      filter(!is.na(canteen)) %>%
      mutate(item_revenue = quantity * unit_price) %>%
      group_by(canteen) %>%
      summarise(
        total_revenue = sum(item_revenue, na.rm = TRUE),
        .groups = "drop"
      ) %>%
      arrange(desc(total_revenue))
    
    validate(
      need(
        nrow(outlet_data) > 0,
        "No outlet sales data available."
      )
    )
    
    ggplot(outlet_data, aes(x = reorder(canteen, total_revenue), y = total_revenue, fill = canteen)) +
      geom_col(width = 0.6) +
      geom_text(
        aes(label = paste0("\u20B9 ", format(round(total_revenue, 0), big.mark = ","))),
        hjust = -0.15,
        color = text_color,
        size = 3.8,
        fontface = "bold"
      ) +
      scale_fill_manual(values = c(
        "Snacks & Shakes Corner" = orange,
        "Chai & Juice Bar" = blue,
        "Food Court (Meals & Tiffins)" = green,
        "General" = "#A78BFA"
      )) +
      scale_y_continuous(
        labels = label_number(prefix = "\u20B9 ", big.mark = ","),
        expand = expansion(mult = c(0, 0.22))
      ) +
      coord_flip() +
      labs(
        x = "Campus Outlet",
        y = "Total Revenue"
      ) +
      chart_theme() +
      theme(legend.position = "none")
    
  }, bg = card_color)
  
  
  # ==========================================================
  # CHART 7: QUANTITY VS ITEM REVENUE SCATTER PLOT
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
  # CHART 8: PEAK ORDERING HOURS & TRAFFIC (AREA CHART)
  # ==========================================================
  
  output$hourly_traffic <- renderPlot({
    
    data <- get_unique_orders(filtered_data())
    
    validate(
      need(
        nrow(data) > 0,
        "No timestamp data available."
      )
    )
    
    hourly_data <- data %>%
      mutate(hour = as.numeric(format(order_time, "%H"))) %>%
      count(hour, name = "order_count")
    
    validate(
      need(
        nrow(hourly_data) > 0,
        "No hourly order data available."
      )
    )
    
    ggplot(hourly_data, aes(x = hour, y = order_count)) +
      geom_area(fill = orange, alpha = 0.25) +
      geom_line(color = orange, linewidth = 1.2) +
      geom_point(color = blue, size = 3.5) +
      scale_x_continuous(
        breaks = seq(0, 23, by = 3),
        labels = function(h) sprintf("%02d:00", as.integer(h))
      ) +
      scale_y_continuous(
        labels = label_number(accuracy = 1),
        expand = expansion(mult = c(0, 0.2))
      ) +
      labs(
        x = "Hour of Day (Campus Time)",
        y = "Orders Placed"
      ) +
      chart_theme()
    
  }, bg = card_color)
  
  
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
        canteen,
        category,
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
      "Canteen Station",
      "Category",
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
        pageLength = 6,
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
