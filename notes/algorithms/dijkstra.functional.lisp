001	(defun get_helper(index li current_index)
002	    (cond
003	        ((eq index current_index) (car li))
004	        ((eq (length li) current_index) nil)
005	        (t (get_helper index (cdr li) (+ 1 current_index)))))
006
007	(defun get_i(index li)
008	        (get_helper index li 0))
009
010	(defun is_element_in_list(element li)
011	    (cond
012	        ((null li) nil)
013	        ((eq element (car li)) t)
014	        (t (is_element_in_list element (cdr li)))))
015
016	(defun flat_helper(x y)
017	    (cond
018	        ((null x) y)
019	        ((atom x) (cons x y))
020	        (t (flat_helper (car x) (flat_helper (cdr x) y)))))
021
022	(defun flat(x)
023	    (flat_helper x nil))
024
025	(defun remove_duplicates_helper(x y)
026	    (cond
027	        ((null x) y)
028	        (t (if (is_element_in_list (car x) y) (remove_duplicates_helper (cdr x) y) (remove_duplicates_helper (cdr x) (cons (car x) y))))))
029
030	(defun remove_duplicates(x)
031	    (remove_duplicates_helper x nil))
032
033	(defun remove_many_items(list_of_items list_of_elements)
034	        (cond
035	            ((null list_of_items) list_of_elements)
036	            (t (remove_many_items (cdr list_of_items) (remove (car list_of_items) list_of_elements)))))
037
038	(defun get_initial_unconnected_nodes_helper(flat_list accum)
039	    (cond
040	        ((null flat_list) accum)
041	        ((atom flat_list) accum)
042	        (t
043	            (get_initial_unconnected_nodes_helper (cdddr flat_list) (cons (car flat_list) (cons (cadr flat_list) accum))))))
044
045	(defun get_initial_unconnected_nodes(start_node list_of_nodes)
046	    (let ((flatten_list_of_nodes (flat list_of_nodes)))
047	        (remove start_node (remove_duplicates (get_initial_unconnected_nodes_helper flatten_list_of_nodes nil)))))
048
049	(defun get_cost(node_1 node_2 list_of_nodes)
050	    (cond
051	        ((null list_of_nodes) nil)
052	        (t  (let((current_tuple (car list_of_nodes)))
053	            (cond
054	                    ((and (eq (car current_tuple) node_1) (eq (cadr current_tuple) node_2)) (caddr current_tuple))
055	                    ((and (eq (car current_tuple) node_2) (eq (cadr current_tuple) node_1)) (caddr current_tuple))
056	                    (t (get_cost node_1 node_2 (cdr list_of_nodes))))))))
057
058	(defun get_list_of_neighbors_helper(current_node list_of_nodes accum)
059	    (cond
060	        ((null list_of_nodes) accum)
061	        (t (let ((current_tuple (car list_of_nodes)))
062	                (cond
063	                    ((eq (car current_tuple) current_node) (get_list_of_neighbors_helper current_node (cdr list_of_nodes) (cons (cadr current_tuple) accum)))
064	                    ((eq (cadr current_tuple) current_node) (get_list_of_neighbors_helper current_node (cdr list_of_nodes) (cons (car current_tuple) accum)))
065	                    (t (get_list_of_neighbors_helper current_node (cdr list_of_nodes) accum)))))))
066
067	(defun get_list_of_neighbors(current_node list_of_nodes)
068	    (get_list_of_neighbors_helper current_node list_of_nodes nil))
069
070	(defun get_list_of_neighbors_in_unconnected_set(current_node list_of_nodes connected_nodes)
071	    (let ((list_of_neighbors (get_list_of_neighbors current_node list_of_nodes)))
072	        (remove_many_items connected_nodes list_of_neighbors)))
073
074	(defun get_cheapest_neighbor_helper(start_node list_of_nodes list_of_neighbors cheapest_node)
075	    (cond
076	        ((null list_of_neighbors) cheapest_node)
077	        (t (let ((current_node (car list_of_neighbors)) (current_cost (get_cost start_node (car list_of_neighbors) list_of_nodes)))
078	                if (< current_cost (get_cost start_node cheapest_node list_of_nodes))
079	                    (get_cheapest_neighbor_helper start_node list_of_nodes (cdr list_of_neighbors) current_node)
080	                    (get_cheapest_neighbor_helper start_node list_of_nodes (cdr list_of_neighbors) cheapest_node)))))
081
082	(defun get_cheapest_neighbor(current_node list_of_nodes visited_nodes)
083	    (let ((list_of_neighbors (remove_many_items visited_nodes (get_list_of_neighbors current_node list_of_nodes))))
084	         get_cheapest_neighbor_helper current_node list_of_nodes list_of_neighbors (car list_of_neighbors)))
085
086	(defun get_next_node_to_inspect_helper(unconnected_nodes tentative_cost chosen_node)
087	    (cond
088	        ((null unconnected_nodes) chosen_node)
089	        (t (
090	            let ((current_node (car unconnected_nodes)))
091	                (if (null (car (assoc current_node tentative_cost)))
092	                    (get_next_node_to_inspect_helper (cdr unconnected_nodes) tentative_cost chosen_node)
093	                    (let ((current_cost (cdr (assoc current_node tentative_cost))))
094	                        (cond
095	                            ((null chosen_node) (get_next_node_to_inspect_helper (cdr unconnected_nodes) tentative_cost current_node))
096	                            ((null current_cost) (get_next_node_to_inspect_helper (cdr unconnected_nodes) tentative_cost chosen_node))
097	                            ((<= current_cost (cdr (assoc chosen_node tentative_cost))) (get_next_node_to_inspect_helper (cdr unconnected_nodes) tentative_cost current_node))
098	                            (t (get_next_node_to_inspect_helper (cdr unconnected_nodes) tentative_cost chosen_node)))))))))
099
100	(defun get_next_node_to_inspect(unconnected_nodes tentative_cost)
101	    (get_next_node_to_inspect_helper unconnected_nodes tentative_cost nil))
102
103	(defun check_neighbors_and_update_helper(current_node list_of_neighbors list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors)
104	    (cond
105	        ((null list_of_neighbors) (dijkstra list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors))
106	        (t (let ((new_cost (+(cdr(assoc current_node tentative_cost)) (get_cost current_node (car list_of_neighbors) list_of_nodes))) (old_cost (cdr (assoc (car list_of_neighbors) tentative_cost))))
107	            (cond
108	                ((null old_cost) (check_neighbors_and_update_helper current_node (cdr list_of_neighbors) list_of_nodes connected_nodes unconnected_nodes desired_tree (acons (car list_of_neighbors) new_cost tentative_cost) (acons (car list_of_neighbors) current_node predecessors)))
109	                ((< new_cost old_cost) (check_neighbors_and_update_helper current_node (cdr list_of_neighbors) list_of_nodes connected_nodes unconnected_nodes desired_tree (acons (car list_of_neighbors) new_cost tentative_cost) (acons (car list_of_neighbors) current_node predecessors)))
110	                (t (check_neighbors_and_update_helper current_node (cdr list_of_neighbors) list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors)))))))
111
112	(defun check_neighbors_and_update(current_node list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors)
113	    (let ((neighbor_list (get_list_of_neighbors_in_unconnected_set current_node list_of_nodes connected_nodes)))
114	        (check_neighbors_and_update_helper current_node neighbor_list list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors)))
115
116	(defun dijkstra(list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors)
117	    (cond
118	        ((null unconnected_nodes) (cons desired_tree tentative_cost))
119	        (t (let((cheapest_neighbor (get_next_node_to_inspect unconnected_nodes tentative_cost)))
120	                (let ((new_desired_tree (cons (cons cheapest_neighbor (cdr(assoc cheapest_neighbor predecessors))) desired_tree)))
121	                    (check_neighbors_and_update cheapest_neighbor list_of_nodes (cons cheapest_neighbor connected_nodes) (remove cheapest_neighbor unconnected_nodes) new_desired_tree tentative_cost predecessors))))))
122
123	(defun organize_individual_entries(list_of_edges accum)
124	    (cond
125	        ((null list_of_edges) accum)
126	        (t (let ((first_entry (caar list_of_edges)) (second_entry (cdar list_of_edges)))
127	            (if (string> first_entry second_entry)
128	                (organize_individual_entries (cdr list_of_edges) (cons (cons second_entry first_entry) accum))
129	                (organize_individual_entries (cdr list_of_edges) (cons (cons first_entry second_entry) accum)))))))
130
131	(defun list_of_edges_cmp(tuple1 tuple2)
132	    (let ((node_a_1 (car tuple1))(node_a_2 (cdr tuple1))(node_b_1 (car tuple2))(node_b_2 (cdr tuple2)))
133	        (cond
134	            ((string= node_a_1 node_b_1)
135	                (cond
136	                    ((string< node_a_2 node_b_2) t)
137	                    (t nil)))
138	            ((string< node_a_1 node_b_1) t)
139	            (t nil))))
140
141	(defun get_tentative_cost(node_1 node_2 tentative_cost)
142	    (let ((cost1 (cdr (assoc node_1 tentative_cost))) (cost2 (cdr (assoc node_2 tentative_cost))))
143	        (if (> cost1 cost2) cost1 cost2)))
144
145	(defun format_final_output(list_of_sorted_edges tentative_cost accum running_sum)
146	    (cond
147	        ((null list_of_sorted_edges) (cons (reverse accum) running_sum))
148	        (t  (let ((node_1 (caar list_of_sorted_edges)) (node_2 (cdar list_of_sorted_edges)))
149	                (let ((cost (get_tentative_cost node_1 node_2 tentative_cost)))
150	                    (let ((prepared_list (cons node_1 (cons node_2 (cons cost nil)))))
151	                        (format_final_output (cdr list_of_sorted_edges) tentative_cost (cons prepared_list accum) (+ running_sum cost))))))))
152
153	(defun cheapest_paths(start_node list_of_nodes)
154	    (let    ((connected_nodes (cons start_node nil))
155	            (unconnected_nodes (get_initial_unconnected_nodes start_node list_of_nodes))
156	            (desired_tree nil)
157	            (tentative_cost (acons start_node 0 nil))
158	            (predecessors (acons start_node nil nil)))
159	            ; Send to UPDATE FUNCTION, NOT DIJKSTRA FUNCTION
160	                (let ((edge_list (check_neighbors_and_update start_node list_of_nodes connected_nodes unconnected_nodes desired_tree tentative_cost predecessors)))
161	                    (let ((second_format (sort (organize_individual_entries (car edge_list) nil) 'list_of_edges_cmp)))
162	                        (let ((third_format (format_final_output second_format (cdr edge_list) nil 0)))
163	                                (append (car third_format) (cons (cdr third_format) nil)))))))