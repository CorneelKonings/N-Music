                                if (line.words != null) {
                                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                    androidx.compose.foundation.layout.FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp, horizontal = 24.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                onLineClick(line.time)
                                            }
                                            .graphicsLayer {
                                                alpha = animatedAlpha
                                                scaleX = animatedScale
                                                scaleY = animatedScale
                                            }
                                            .blur(animatedBlur),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        line.words.forEach { word ->
                                            if (word.text == " " || word.text == "\n") {
                                                Text(
                                                    text = word.text,
                                                    style = TextStyle(fontSize = 24.sp)
                                                )
                                                return@forEach
                                            }
                                            
                                            val wordStartMs = (word.startTime * 1000).toLong()
                                            val wordEndMs = (word.endTime * 1000).toLong()
                                            val wordDuration = (wordEndMs - wordStartMs).coerceAtLeast(1L)
                                            
                                            Text(
                                                text = word.text,
                                                color = Color.White,
                                                style = TextStyle(
                                                    fontSize = 24.sp,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                                    shadow = SoftTextShadow
                                                ),
                                                modifier = Modifier
                                                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                                    .drawWithContent {
                                                        drawContent()
                                                        
                                                        val currentMs = currentMsState.value + syncOffset
                                                        val progress = when {
                                                            currentMs >= wordEndMs -> 1f
                                                            currentMs <= wordStartMs -> 0f
                                                            else -> ((currentMs - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
                                                        }
                                                        
                                                        val edgeWidth = 8.dp.toPx()
                                                        val center = size.width * progress
                                                        
                                                        drawRect(
                                                            brush = Brush.horizontalGradient(
                                                                colors = listOf(Color.White, Color.White.copy(alpha = 0.35f)),
                                                                startX = center - edgeWidth,
                                                                endX = center + edgeWidth
                                                            ),
                                                            blendMode = BlendMode.DstIn
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = line.text,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp, horizontal = 24.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                onLineClick(line.time)
                                            }
                                            .graphicsLayer {
                                                alpha = animatedAlpha
                                                scaleX = animatedScale
                                                scaleY = animatedScale
                                            }
                                            .blur(animatedBlur),
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                            shadow = SoftTextShadow
                                        )
                                    )
                                }
