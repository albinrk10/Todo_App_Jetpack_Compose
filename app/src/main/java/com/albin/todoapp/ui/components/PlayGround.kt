package com.albin.todoapp.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albin.todoapp.ui.theme.TodoAppTheme


//Text color
@Composable
    fun HelloWorld(
    modifier: Modifier = Modifier,
    ) {
        Text(
            modifier = modifier
                
                .background(
                    color = Color.Red
                )
                .padding(16.dp),
            text = "Hello, Albin",
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
    }

@Preview(
    showBackground = true
)
@Composable
fun HelloWorldPreview() {
   TodoAppTheme {
         HelloWorld()
   }
}
//ClickableText
@Composable
fun ClickbleTextExample(
    modifier: Modifier = Modifier,
){
    var  text by remember {
        mutableStateOf("Click me!")
    }
    Text(
        text = text,
        fontSize = 16.sp,
        modifier = modifier
            .clickable {
                text = "This is a new text!"
            }
    )
}
@Preview(
    showBackground = true
)
@Composable
fun ClickbleTextExamplePreview() {
    TodoAppTheme {
        ClickbleTextExample()
    }
}
///icon
@Composable
fun IconExample(
    modifier: Modifier = Modifier,
    iconContainer: IconContainer
){
    Icon(
        imageVector = iconContainer.icon,
        contentDescription = "Favorite Icon",
        modifier = modifier
            .size(48.dp)

            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = CircleShape

            )
            .padding(8.dp)
    )
}
@Preview(
    showBackground = true
)
@Composable
fun IconExamplePreview(
    @PreviewParameter(IconProvider::class) icon: IconContainer
) {
    TodoAppTheme {
        IconExample(
            iconContainer = icon
        )

    }
}
//Row
@Composable
fun RowView(){
   Row(
         modifier = Modifier.padding(16.dp),
       verticalAlignment = Alignment.CenterVertically,
       horizontalArrangement = Arrangement.spacedBy(8.dp)

   ) {
       HelloWorld()

       IconExample(
              iconContainer = IconContainer(Icons.Default.Favorite)
       )
   }
}

@Preview(
    showBackground = true
)
@Composable
fun RowViewPreview() {
    TodoAppTheme {
        RowView()
    }
}
//Column
@Composable
fun ColumnView(){
    Column(
       // modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HelloWorld()
        IconExample(
            iconContainer = IconContainer(Icons.Default.Favorite) )
        }

    }


@Preview(
    showBackground = true
)
@Composable
fun ColumnViewPreview() {
    TodoAppTheme {
        ColumnView()
    }
}