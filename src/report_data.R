
library(ggplot2)
library(dplyr)
library(tidyr)
library(cowplot)
library(gridExtra)

df = read.table(
  file = "temp/datatable.tsv",
  header = TRUE
)

df = df %>%
  group_by(id) %>%
  mutate(
    date = first(date),
    time = first(time),
    temperature_celsius = median(temperature_celsius),
    humidity_perc = median(humidity_perc),
    altitude_m = median(altitude_m),
    pressure_hPa = as.integer(median(pressure_hPa)),
    weather = first(weather)
  ) %>%
  distinct %>%
  mutate(
    datetime = paste0(date, ":", gsub("(..[:]..).*", "\\1", time))
  )

p = df %>%
  gather(variable, value, -id, -date, -time, -weather, -datetime) %>%
  ggplot(aes(x = datetime, y = value, group = variable, color = weather)) +
  geom_line(size = 1.5) +
  labs(x = "", y = "") +
  theme_cowplot() +
  theme(
    axis.text.x = element_text(angle=90, hjust = 1, vjust = 0.5)
  ) +
  facet_wrap(~ variable, ncol = 1, scales = "free_y")

ggsave(
  "image/report_data.png",
  p,
  width = 12,
  height = 6,
  units = "in"
)
