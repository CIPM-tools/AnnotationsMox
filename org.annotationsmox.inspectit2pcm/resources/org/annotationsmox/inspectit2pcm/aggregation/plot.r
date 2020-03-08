plot.eval <- function(rdsFile, outputFile) {
	library(data.table)
	library(ggplot2)
	library(dplyr)
	
	mm <- readRDS(rdsFile)
	
	mm$m.boundary.upper <- as.numeric(mm$m.boundary.upper)
	mm$m.boundary.lower <- as.numeric(mm$m.boundary.lower)
	
	buckets <- mm %>% 
		filter(what == "bucket.probability") %>% 
		select(relfreq=value, upper=m.boundary.upper, lower=m.boundary.lower) %>%
		mutate(width = upper - lower) %>%
		mutate(density = relfreq / width)
	
	buckets[1]$lower <- buckets[1]$upper
	buckets[1]$width <- 0
	lastrow <- tail(buckets, 1)
	additional.row <- data.table(relfreq=0, upper=lastrow$upper, lower=lastrow$upper, width=0, density=0)
	buckets <- rbind(buckets, additional.row)
	
	plot <- ggplot(mm, aes(x=value)) + 
		  geom_histogram(data=mm[what=="measured"], aes(y=..density..), bins=100, alpha=0.3) +
		  #geom_histogram(data=mm[what=="sampled"], b, alpha=0, color="red", size=.7, position="identity") +
		  geom_step(data=buckets, aes(x=lower, y=density, direction='vh'), size=1) +
		  xlab("Value") + 
		  ylab("Density") +
		  theme_bw() + 
		  theme(legend.title = element_blank(), text=element_text(size=10))
	  
	ggsave(filename=outputFile, plot=plot, device="pdf", width=30, height=30, units="cm")
}