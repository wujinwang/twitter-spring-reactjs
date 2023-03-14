import { makeStyles, Theme } from "@material-ui/core";

export const useTextSearchResultStyles = makeStyles((theme: Theme) => ({
    searchTextResult: {
        height: 80,
        "& svg": {
            marginRight: 15,
            width: "1.8rem",
            height: "1.8rem",
            fill: theme.palette.text.primary
        }
    }
}));
