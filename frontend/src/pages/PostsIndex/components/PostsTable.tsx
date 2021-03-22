import React, { useEffect, useState } from "react";
import { PostsIndexResponse } from "../../../utils/types/posts";
import useAutoCloseSnack from "../../../hooks/useAutoCloseSnack";
import { api } from "../../../utils/Api";
import MaterialTable from "material-table";

const mock = [
  {
    id: 1,
    title: "test title",
    url: "https:yahoo.com",
    author: "test author",
    postedAt: 10000,
    createdAt: 1222
  },
  {
    id: 2,
    title: "sample title",
    url: "https:google.com",
    author: "sampler",
    postedAt: 100,
    createdAt: 122333
  }
];

export const PostsTable: React.FC<unknown> = props => {
  const [fetchedPosts, setFetchedPosts] = useState<PostsIndexResponse["data"]>(
    []
  );

  const { successSnack, errorSnack } = useAutoCloseSnack();
  const deletePosts = (postIds: number[]) =>
    api
      .delete("http://localhost:9000/posts", { data: postIds })
      .then(() => successSnack("eee"))
      .catch(e => {
        errorSnack(e.message);
      });

  useEffect(() => {
    api.get<PostsIndexResponse>("http://localhost:9000/posts").then(r =>
      // setFetchedPosts(r.data.data)
      setFetchedPosts(mock)
    );
  }, []);
  return (
    <MaterialTable
      style={{ width: "90%", margin: "auto", padding: "0 16px" }}
      columns={[
        {
          title: "タイトル",
          field: "title"
        },
        { title: "URL", field: "url" },
        { title: "著者", field: "author" },
        { title: "投稿日時", field: "postedAt", type: "numeric" },
        { title: "登録日時", field: "createdAt", type: "numeric" },
        { title: "id", field: "id", hidden: true }
      ]}
      data={fetchedPosts}
      title={"記事一覧"}
      options={{
        selection: true
      }}
      actions={[
        {
          icon: "delete",
          onClick: (evt, data) => {
            if (data instanceof Array) {
              deletePosts(data.map(d => d.id));
            } else {
              deletePosts([data.id]);
            }
          }
        }
      ]}
      localization={{
        toolbar: {
          nRowsSelected: "{0}件を選択中"
        }
      }}
    />
  );
};
