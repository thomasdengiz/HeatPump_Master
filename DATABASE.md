# 🗄️ Database Schema

This project includes a pre-populated SQLite database defining the levels and their parameters.

Below is a description of each table and its columns.

---

## 📋 Table: Level_Elements

| Column   | Type     | Description                                     |
|----------|----------|-------------------------------------------------|
| Level    | INTEGER  | Level number (part of primary key)              |
| Timeslot | INTEGER  | Timeslot identifier within the level (part of primary key) |
| PV       | INTEGER  | Value related to photovoltaic electricity game event rectangles |
| Wind     | INTEGER  | Value related to wind energy electricity game event rectangles |
| Fossil   | INTEGER  | Value related to fossil fuel electricity game event rectangles |

The values for PV, Wind, Fossil indicate how long the respective game event rectangles are that are being displayed at the specified timeslots. 


## 📋 Table: Level_Infos

| Column                               | Type    | Description                                               |
|--------------------------------------|---------|-----------------------------------------------------------|
| Level_Number                         | INTEGER | Unique level number (primary key)                         |
| Needed_Percentage_Score_For_The_Level| REAL    | Minimum score percentage required to complete the level as a percentage of the Baseline_Score_For_The_Level   |
| Baseline_Score_For_The_Level         | REAL    | Baseline score value for the level                        |
| Speed_Multiplicator                  | REAL    | Multiplier affecting the gameplay speed  (lower values make game event rectangles move faster)  |

---
